package com.stpauls.apostolicreferencebible.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipInputStream

class VplDbImporter(private val context: Context) {

    companion object {
        private const val TAG = "VplDbImporter"
        private val VPL_REGEX = Regex("""^\uFEFF?([a-zA-Z0-9_-]+)[\s\t]+(\d+)[:.](\d+)[\s\t]+(.+)$""")
    }

    private fun enablePermissiveSSL(connection: HttpURLConnection) {
        if (connection is javax.net.ssl.HttpsURLConnection) {
            try {
                val trustAllCerts = arrayOf<javax.net.ssl.TrustManager>(object : javax.net.ssl.X509TrustManager {
                    override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
                    override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}
                    override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}
                })

                val sc = javax.net.ssl.SSLContext.getInstance("TLS")
                sc.init(null, trustAllCerts, java.security.SecureRandom())
                connection.sslSocketFactory = sc.socketFactory
                connection.hostnameVerifier = javax.net.ssl.HostnameVerifier { _, _ -> true }
            } catch (e: Exception) {
                Log.e(TAG, "Error setting up permissive SSL", e)
            }
        }
    }

    private fun openConnectionSafe(urlString: String): HttpURLConnection {
        var conn = URL(urlString).openConnection() as HttpURLConnection
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
        conn.connectTimeout = 15000
        conn.readTimeout = 30000
        conn.instanceFollowRedirects = true

        try {
            conn.connect()
        } catch (e: Exception) {
            // Fallback for legacy Android emulators/devices missing Let's Encrypt ISRG Root X1 cert
            conn.disconnect()
            conn = URL(urlString).openConnection() as HttpURLConnection
            enablePermissiveSSL(conn)
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            conn.connectTimeout = 15000
            conn.readTimeout = 30000
            conn.instanceFollowRedirects = true
            conn.connect()
        }

        return conn
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

            // 1. Download VPL zip file with User-Agent & redirect handling
            var currentUrl = item.vplUrl
            var conn = openConnectionSafe(currentUrl)

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
                conn = openConnectionSafe(currentUrl)
                status = conn.responseCode
                redirects++
            }

            if (status == HttpURLConnection.HTTP_NOT_FOUND) {
                onError("The ${item.title} file is not available in VPL format on ebible.org")
                return
            } else if (status != HttpURLConnection.HTTP_OK) {
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

            var insertedCount = 0
            val dbPath = context.getDatabasePath(dbFileName)
            val dbHelper = BibleDatabaseHelper(context)
            dbHelper.closeDatabase(dbFileName)

            if (dbPath.exists()) dbPath.delete()
            dbPath.parentFile?.mkdirs()

            val localizedBookNames = mutableMapOf<String, String>()

            // 2. Unzip and parse VPL text file
            ZipInputStream(tempZipFile.inputStream()).use { zipInput ->
                var entry = zipInput.nextEntry
                var foundVplEntry = false

                while (entry != null) {
                    val name = entry.name.lowercase()
                    if (!entry.isDirectory && (name.endsWith(".txt") || name.endsWith(".vpl") || name.endsWith(".usfm"))) {
                        foundVplEntry = true
                        val reader = BufferedReader(InputStreamReader(zipInput, Charsets.UTF_8))

                        onProgress("Building database for ${item.title}...")

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
                        db.execSQL(
                            """
                            CREATE TABLE IF NOT EXISTS book_names (
                                book_code TEXT PRIMARY KEY,
                                localized_name TEXT NOT NULL
                            );
                            """.trimIndent()
                        )

                        db.beginTransaction()
                        val stmt = db.compileStatement("INSERT INTO verses (book, chapter, verse, text) VALUES (?, ?, ?, ?);")
                        var currentHeaderBook = ""

                        try {
                            var line = reader.readLine()
                            while (line != null) {
                                val trimmed = line.replace("\uFEFF", "").trim()
                                if (trimmed.isNotEmpty()) {
                                    if (trimmed.startsWith("\\id ")) {
                                        val parts = trimmed.substringAfter("\\id ").trim().split(Regex("""[\s\t]+"""))
                                        if (parts.isNotEmpty()) {
                                            currentHeaderBook = dbHelper.normalizeBookCode(parts[0])
                                            if (parts.size > 1) {
                                                val possibleName = parts.subList(1, parts.size).joinToString(" ").trim()
                                                if (possibleName.isNotEmpty() && !possibleName.matches(Regex("""\d+"""))) {
                                                    localizedBookNames[currentHeaderBook] = possibleName
                                                }
                                            }
                                        }
                                    } else if (trimmed.startsWith("\\h ") || trimmed.startsWith("\\toc1 ") || trimmed.startsWith("\\toc2 ")) {
                                        val titleName = trimmed.substringAfter(' ').trim()
                                        if (titleName.isNotEmpty() && currentHeaderBook.isNotEmpty()) {
                                            localizedBookNames[currentHeaderBook] = titleName
                                        }
                                    } else if (trimmed.startsWith("#") && trimmed.contains("=")) {
                                        val rawBook = trimmed.substringAfter("#").substringBefore("=").trim()
                                        val locTitle = trimmed.substringAfter("=").trim()
                                        if (rawBook.isNotEmpty() && locTitle.isNotEmpty()) {
                                            val norm = dbHelper.normalizeBookCode(rawBook)
                                            localizedBookNames[norm] = locTitle
                                        }
                                    } else if (!trimmed.startsWith("#") && !trimmed.startsWith("\\")) {
                                        val match = VPL_REGEX.find(trimmed)
                                        if (match != null) {
                                            val bookRaw = match.groupValues[1]
                                            val chapStr = match.groupValues[2]
                                            val verseStr = match.groupValues[3]
                                            val verseText = match.groupValues[4].trim()

                                            val cNum = chapStr.toIntOrNull()
                                            val vNum = verseStr.toIntOrNull()
                                            val normalizedBook = dbHelper.normalizeBookCode(bookRaw)
                                            currentHeaderBook = normalizedBook

                                            if (cNum != null && vNum != null && verseText.isNotEmpty()) {
                                                stmt.clearBindings()
                                                stmt.bindString(1, normalizedBook)
                                                stmt.bindLong(2, cNum.toLong())
                                                stmt.bindLong(3, vNum.toLong())
                                                stmt.bindString(4, verseText)
                                                stmt.executeInsert()
                                                insertedCount++
                                            }
                                        } else {
                                            // Secondary fallback tokenizer for non-standard separators
                                            val parts = trimmed.split(Regex("""[\s\t]+"""), limit = 3)
                                            if (parts.size >= 3) {
                                                val refPart = parts[1]
                                                val colonIdx = refPart.indexOf(':')
                                                if (colonIdx > 0) {
                                                    val bookRaw = parts[0]
                                                    val cNum = refPart.substring(0, colonIdx).toIntOrNull()
                                                    val vNum = refPart.substring(colonIdx + 1).toIntOrNull()
                                                    val verseText = parts[2].trim()
                                                    val normalizedBook = dbHelper.normalizeBookCode(bookRaw)
                                                    currentHeaderBook = normalizedBook

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
                                }
                                line = reader.readLine()
                            }
                            db.setTransactionSuccessful()
                        } finally {
                            db.endTransaction()
                            stmt.close()
                        }

                        if (localizedBookNames.isNotEmpty()) {
                            val bookStmt = db.compileStatement("INSERT OR REPLACE INTO book_names (book_code, localized_name) VALUES (?, ?);")
                            db.beginTransaction()
                            try {
                                for ((bCode, locName) in localizedBookNames) {
                                    bookStmt.clearBindings()
                                    bookStmt.bindString(1, bCode)
                                    bookStmt.bindString(2, locName)
                                    bookStmt.executeInsert()
                                }
                                db.setTransactionSuccessful()
                            } finally {
                                db.endTransaction()
                                bookStmt.close()
                            }
                        }

                        db.close()

                        if (insertedCount >= 100) {
                            break
                        }
                    }
                    zipInput.closeEntry()
                    entry = zipInput.nextEntry
                }

                if (!foundVplEntry) {
                    if (dbPath.exists()) dbPath.delete()
                    if (tempZipFile.exists()) tempZipFile.delete()
                    onError("No valid VPL text file found inside zip archive")
                    return
                }
            }

            if (insertedCount < 100) {
                if (dbPath.exists()) dbPath.delete()
                if (tempZipFile.exists()) tempZipFile.delete()
                onError("Failed to parse verses from file (only $insertedCount verses found). The VPL format may be unsupported.")
                return
            }

            Log.d(TAG, "Successfully inserted $insertedCount verses into $dbFileName")

            // 3. Immediately delete temporary zip file from cache
            if (tempZipFile.exists()) tempZipFile.delete()

            // 4. Fetch official USFM archive to extract native localized book names if book_names has < 10 entries
            if (localizedBookNames.size < 10) {
                onProgress("Fetching localized book titles for ${item.title}...")
                try {
                    fetchAndPopulateUsfmBookNames(item.id, dbPath, dbHelper)
                } catch (t: Throwable) {
                    Log.w(TAG, "USFM book name fetch skipped for ${item.id}: ${t.message}")
                }
            }

            onSuccess(dbFileName)
        } catch (e: Exception) {
            Log.e(TAG, "Error importing VPL Bible", e)
            val dbPath = context.getDatabasePath(dbFileName)
            if (dbPath.exists()) dbPath.delete()
            if (tempZipFile.exists()) tempZipFile.delete()
            onError("Error: ${e.localizedMessage ?: "Failed to process Bible file"}")
        }
    }

    private fun fetchAndPopulateUsfmBookNames(
        itemId: String,
        dbPath: File,
        dbHelper: BibleDatabaseHelper
    ) {
        val usfmUrl = "https://ebible.org/Scriptures/${itemId}_usfm.zip"
        val tempUsfmFile = File(context.cacheDir, "temp_${itemId}_usfm.zip")

        try {
            val conn = openConnectionSafe(usfmUrl)

            if (conn.responseCode != HttpURLConnection.HTTP_OK) {
                conn.disconnect()
                return
            }

            conn.inputStream.use { input ->
                FileOutputStream(tempUsfmFile).use { output ->
                    val buffer = ByteArray(8192)
                    var read: Int
                    while (input.read(buffer).also { read = it } > 0) {
                        output.write(buffer, 0, read)
                    }
                    output.flush()
                }
            }
            conn.disconnect()

            val localizedMap = mutableMapOf<String, String>()

            ZipInputStream(tempUsfmFile.inputStream()).use { zipInput ->
                var entry = zipInput.nextEntry
                while (entry != null) {
                    val name = entry.name.lowercase()
                    if (!entry.isDirectory && (name.endsWith(".usfm") || name.endsWith(".sfm") || name.endsWith(".txt"))) {
                        val reader = BufferedReader(InputStreamReader(zipInput, Charsets.UTF_8))
                        var currentBookCode = ""
                        var line = reader.readLine()
                        var linesRead = 0

                        while (line != null && linesRead < 100) {
                            linesRead++
                            val trimmed = line.replace("\uFEFF", "").trim()
                            if (trimmed.startsWith("\\id ")) {
                                val parts = trimmed.substringAfter("\\id ").trim().split(Regex("""[\s\t]+"""))
                                if (parts.isNotEmpty()) {
                                    currentBookCode = dbHelper.normalizeBookCode(parts[0])
                                }
                            } else if (trimmed.startsWith("\\h ") || trimmed.startsWith("\\toc1 ") || trimmed.startsWith("\\toc2 ")) {
                                val titleName = trimmed.substringAfter(' ').trim()
                                if (titleName.isNotEmpty() && currentBookCode.isNotEmpty() && !titleName.matches(Regex("""\d+"""))) {
                                    localizedMap[currentBookCode] = titleName
                                }
                            }
                            line = reader.readLine()
                        }
                    }
                    zipInput.closeEntry()
                    entry = zipInput.nextEntry
                }
            }

            if (tempUsfmFile.exists()) tempUsfmFile.delete()

            if (localizedMap.isNotEmpty() && dbPath.exists()) {
                dbHelper.closeDatabase(dbPath.name)
                val db = SQLiteDatabase.openOrCreateDatabase(dbPath, null)
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS book_names (
                        book_code TEXT PRIMARY KEY,
                        localized_name TEXT NOT NULL
                    );
                    """.trimIndent()
                )
                val bookStmt = db.compileStatement("INSERT OR REPLACE INTO book_names (book_code, localized_name) VALUES (?, ?);")
                db.beginTransaction()
                try {
                    for ((bCode, locName) in localizedMap) {
                        bookStmt.clearBindings()
                        bookStmt.bindString(1, bCode)
                        bookStmt.bindString(2, locName)
                        bookStmt.executeInsert()
                    }
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                    bookStmt.close()
                    db.close()
                }
                Log.d(TAG, "Successfully populated ${localizedMap.size} USFM book names for $itemId")
            }
        } catch (e: Exception) {
            Log.w(TAG, "USFM book name fetch skipped for $itemId: ${e.message}")
            if (tempUsfmFile.exists()) tempUsfmFile.delete()
        }
    }
}
