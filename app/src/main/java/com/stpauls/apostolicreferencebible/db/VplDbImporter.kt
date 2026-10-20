package com.stpauls.apostolicreferencebible.db

import android.annotation.SuppressLint
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.zip.ZipInputStream
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class VplDbImporter(private val context: Context) {

    companion object {
        private const val TAG = "VplDbImporter"
    }

    @SuppressLint("TrustAllX509TrustManager", "CustomX509TrustManager")
    private fun enablePermissiveSSL(connection: HttpURLConnection) {
        if (connection is HttpsURLConnection) {
            try {
                val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                })

                val sc = SSLContext.getInstance("TLS")
                sc.init(null, trustAllCerts, SecureRandom())
                connection.sslSocketFactory = sc.socketFactory
                connection.hostnameVerifier = HostnameVerifier { _, _ -> true }
            } catch (e: Exception) {
                Log.e(TAG, "Error setting up permissive SSL", e)
            }
        }
    }

    fun downloadAndBuildDb(
        item: EbibleCatalogItem,
        onProgress: (statusText: String) -> Unit,
        onSuccess: (dbName: String) -> Unit,
        onError: (errorMessage: String) -> Unit
    ) {
        val dbFileName = "ebible_${item.id.lowercase().replace("-", "_")}.db"
        val tempZipFile = File(context.cacheDir, "temp_${item.id}.zip")

        try {
            onProgress("Downloading ${item.title}...")

            // 1. Download VPL zip file with permissive SSL, User-Agent & redirect handling
            var currentUrl = item.vplUrl
            var conn = URL(currentUrl).openConnection() as HttpURLConnection
            enablePermissiveSSL(conn)
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            conn.connectTimeout = 15000
            conn.readTimeout = 30000
            conn.instanceFollowRedirects = true
            conn.connect()

            var status = conn.responseCode
            var redirects = 0

            while ((status == HttpURLConnection.HTTP_MOVED_TEMP ||
                    status == HttpURLConnection.HTTP_MOVED_PERM ||
                    status == HttpURLConnection.HTTP_SEE_OTHER ||
                    status == 307 || status == 308) && redirects < 5) {
                val newUrl = conn.getHeaderField("Location")
                conn.disconnect()
                if (newUrl.isNullOrBlank()) break
                currentUrl = newUrl
                conn = URL(currentUrl).openConnection() as HttpURLConnection
                enablePermissiveSSL(conn)
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                conn.connectTimeout = 15000
                conn.readTimeout = 30000
                conn.instanceFollowRedirects = true
                conn.connect()
                status = conn.responseCode
                redirects++
            }

            if (status != HttpURLConnection.HTTP_OK) {
                onError("HTTP $status error while downloading ${item.title}")
                return
            }

            conn.inputStream.use { input ->
                FileOutputStream(tempZipFile).use { output ->
                    val buffer = ByteArray(8192)
                    var read: Int
                    while (input.read(buffer).also { read = it } > 0) {
                        output.write(buffer, 0, read)
                    }
                    output.flush()
                }
            }
            conn.disconnect()

            onProgress("Extracting & validating Bible text...")

            // 2. Unzip and find VPL text file inside
            var vplReader: BufferedReader? = null

            ZipInputStream(tempZipFile.inputStream()).use { zipInput ->
                var entry = zipInput.nextEntry
                while (entry != null) {
                    val name = entry.name.lowercase()
                    if (name.endsWith(".txt") || name.endsWith(".vpl")) {
                        vplReader = BufferedReader(InputStreamReader(zipInput, Charsets.UTF_8))
                        break
                    }
                    zipInput.closeEntry()
                    entry = zipInput.nextEntry
                }

                if (vplReader == null) {
                    tempZipFile.delete()
                    onError("No valid VPL text file found inside zip archive")
                    return
                }

                onProgress("Building database for ${item.title}...")

                // 3. Create destination SQLite database file
                val dbPath = context.getDatabasePath(dbFileName)
                if (dbPath.exists()) dbPath.delete()
                dbPath.parentFile?.mkdirs()

                val db = SQLiteDatabase.openOrCreateDatabase(dbPath, null)

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS verses (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        book TEXT NOT NULL,
                        chapter INTEGER NOT NULL,
                        verse INTEGER NOT NULL,
                        text TEXT NOT NULL
                    );
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_book_chap ON verses(book, chapter);")

                db.beginTransaction()
                var insertedCount = 0
                val stmt = db.compileStatement("INSERT INTO verses (book, chapter, verse, text) VALUES (?, ?, ?, ?);")

                try {
                    var line = vplReader?.readLine()

                    while (line != null) {
                        val trimmed = line.trim()
                        if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                            val spaceIdx = trimmed.indexOf(' ')
                            if (spaceIdx > 0) {
                                val bookRaw = trimmed.substring(0, spaceIdx).trim()
                                val rest = trimmed.substring(spaceIdx + 1).trim()
                                val colonIdx = rest.indexOf(':')

                                if (colonIdx > 0) {
                                    val chapStr = rest.substring(0, colonIdx).trim()
                                    val verseAndText = rest.substring(colonIdx + 1).trim()

                                    val verseSpaceIdx = verseAndText.indexOf(' ')
                                    if (verseSpaceIdx > 0) {
                                        val verseStr = verseAndText.substring(0, verseSpaceIdx).trim()
                                        val verseText = verseAndText.substring(verseSpaceIdx + 1).trim()

                                        val cNum = chapStr.toIntOrNull()
                                        val vNum = verseStr.toIntOrNull()
                                        val normalizedBook = BibleDatabaseHelper(context).normalizeBookCode(bookRaw)

                                        if (cNum != null && vNum != null && verseText.isNotEmpty()) {
                                            stmt.clearBindings()
                                            stmt.bindString(1, normalizedBook)
                                            stmt.bindLong(2, cNum.toLong())
                                            stmt.bindLong(3, vNum.toLong())
                                            stmt.bindString(4, verseText)
                                            stmt.executeInsert()
                                            insertedCount++
                                        }
                                    }
                                }
                            }
                        }
                        line = vplReader?.readLine()
                    }

                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                    stmt.close()
                    db.close()
                }

                if (insertedCount < 100) {
                    dbPath.delete()
                    tempZipFile.delete()
                    onError("Failed to parse verses from file (Corrupted VPL structure)")
                    return
                }

                Log.d(TAG, "Successfully inserted $insertedCount verses into $dbFileName")
            }

            // 4. Immediately delete temporary zip file from cache
            if (tempZipFile.exists()) tempZipFile.delete()

            onSuccess(dbFileName)
        } catch (e: Exception) {
            Log.e(TAG, "Error importing VPL Bible", e)
            if (tempZipFile.exists()) tempZipFile.delete()
            onError("Error: ${e.localizedMessage ?: "Failed to process Bible file"}")
        }
    }
}
