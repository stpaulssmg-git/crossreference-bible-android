package com.stpauls.apostolicreferencebible.db

data class Verse(
    val id: Long = 0,
    val book: String,
    val chapter: Int,
    val verse: Int,
    val text: String
)

data class CrossReference(
    val id: Long = 0,
    val book: String,
    val chapter: Int,
    val verse: Int,
    val anchor: String,
    val references: String
)

data class CrossRefDetail(
    val reference: String,
    val previewText: String?
)

data class BookInfo(
    val code: String,
    val name: String,
    val totalChapters: Int
)

data class SearchResult(
    val bookCode: String,
    val bookName: String,
    val chapter: Int,
    val verse: Int,
    val text: String
)

data class TranslationInfo(
    val code: String,
    val name: String,
    val dbName: String,
    val isPrimary: Boolean,
    val isEnabled: Boolean
)

data class TranslationVerse(
    val translationCode: String,
    val translationName: String,
    val text: String
)

val INSTALLED_TRANSLATIONS = listOf(
    TranslationInfo("KJV", "King James Version", "kjv.db", isPrimary = true, isEnabled = true),
    TranslationInfo("ABE", "Apostolic Polyglot (English)", "abe.db", isPrimary = false, isEnabled = true),
    TranslationInfo("ABG", "Apostolic Polyglot (Greek)", "abg.db", isPrimary = false, isEnabled = true),
    TranslationInfo("LXX", "Thomson Septuagint (English)", "lxx.db", isPrimary = false, isEnabled = false),
    TranslationInfo("H/G", "Hebrew OT / Greek TR NT", "hgb.db", isPrimary = false, isEnabled = false),
    TranslationInfo("T4T", "Translation for Translators", "t4t.db", isPrimary = false, isEnabled = false),
    TranslationInfo("NAS", "NASB (English)", "nas.db", isPrimary = false, isEnabled = false),
    TranslationInfo("KAN", "Kannada IRV Bible", "kan.db", isPrimary = false, isEnabled = true),
    TranslationInfo("TEL", "Telugu IRV Bible", "tel.db", isPrimary = false, isEnabled = false),
    TranslationInfo("TAM", "Tamil IRV Bible", "tam.db", isPrimary = false, isEnabled = false),
    TranslationInfo("MAL", "Malayalam IRV Bible", "mal.db", isPrimary = false, isEnabled = false),
    TranslationInfo("HIN", "Hindi IRV Bible", "hin.db", isPrimary = false, isEnabled = false),
    TranslationInfo("SPA", "Spanish Reina Valera Gomez", "spa.db", isPrimary = false, isEnabled = false)
)

val TRANSLATION_MAP = INSTALLED_TRANSLATIONS.associate { it.code to it.name }

val BIBLE_BOOKS = listOf(
    BookInfo("GEN", "Genesis", 50),
    BookInfo("EXO", "Exodus", 40),
    BookInfo("LEV", "Leviticus", 27),
    BookInfo("NUM", "Numbers", 36),
    BookInfo("DEU", "Deuteronomy", 34),
    BookInfo("JOS", "Joshua", 24),
    BookInfo("JDG", "Judges", 21),
    BookInfo("RUT", "Ruth", 4),
    BookInfo("1SA", "1 Samuel", 31),
    BookInfo("2SA", "2 Samuel", 24),
    BookInfo("1KI", "1 Kings", 22),
    BookInfo("2KI", "2 Kings", 25),
    BookInfo("1CH", "1 Chronicles", 29),
    BookInfo("2CH", "2 Chronicles", 36),
    BookInfo("EZR", "Ezra", 10),
    BookInfo("NEH", "Nehemiah", 13),
    BookInfo("EST", "Esther", 10),
    BookInfo("JOB", "Job", 42),
    BookInfo("PSA", "Psalms", 150),
    BookInfo("PRO", "Proverbs", 31),
    BookInfo("ECC", "Ecclesiastes", 12),
    BookInfo("SNG", "Song of Solomon", 8),
    BookInfo("ISA", "Isaiah", 66),
    BookInfo("JER", "Jeremiah", 52),
    BookInfo("LAM", "Lamentations", 5),
    BookInfo("EZK", "Ezekiel", 48),
    BookInfo("DAN", "Daniel", 12),
    BookInfo("HOS", "Hosea", 14),
    BookInfo("JOL", "Joel", 3),
    BookInfo("AMO", "Amos", 9),
    BookInfo("OBA", "Obadiah", 1),
    BookInfo("JON", "Jonah", 4),
    BookInfo("MIC", "Micah", 7),
    BookInfo("NAH", "Nahum", 3),
    BookInfo("HAB", "Habakkuk", 3),
    BookInfo("ZEP", "Zephaniah", 3),
    BookInfo("HAG", "Haggai", 2),
    BookInfo("ZEC", "Zechariah", 14),
    BookInfo("MAL", "Malachi", 4),
    BookInfo("MAT", "Matthew", 28),
    BookInfo("MRK", "Mark", 16),
    BookInfo("LUK", "Luke", 24),
    BookInfo("JHN", "John", 21),
    BookInfo("ACT", "Acts", 28),
    BookInfo("ROM", "Romans", 16),
    BookInfo("1CO", "1 Corinthians", 16),
    BookInfo("2CO", "2 Corinthians", 13),
    BookInfo("GAL", "Galatians", 6),
    BookInfo("EPH", "Ephesians", 6),
    BookInfo("PHP", "Philippians", 4),
    BookInfo("COL", "Colossians", 4),
    BookInfo("1TH", "1 Thessalonians", 5),
    BookInfo("2TH", "2 Thessalonians", 3),
    BookInfo("1TI", "1 Timothy", 6),
    BookInfo("2TI", "2 Timothy", 4),
    BookInfo("TIT", "Titus", 3),
    BookInfo("PHM", "Philemon", 1),
    BookInfo("HEB", "Hebrews", 13),
    BookInfo("JAS", "James", 5),
    BookInfo("1PE", "1 Peter", 5),
    BookInfo("2PE", "2 Peter", 3),
    BookInfo("1JN", "1 John", 5),
    BookInfo("2JN", "2 John", 1),
    BookInfo("3JN", "3 John", 1),
    BookInfo("JUD", "Jude", 1),
    BookInfo("REV", "Revelation", 22)
)

val BOOK_NAME_MAP = BIBLE_BOOKS.associate { it.code to it.name }

val KAN_BOOK_NAME_MAP = mapOf(
    "GEN" to "ಆದಿಕಾಂಡ", "EXO" to "ವಿಮೋಚನಕಾಂಡ", "LEV" to "ಯಾಜಕಕಾಂಡ", "NUM" to "ಅರಣ್ಯಕಾಂಡ", "DEU" to "ಧರ್ಮೋಪದೇಶಕಾಂಡ",
    "JOS" to "ಯೆಹೋಶುವ", "JDG" to "ನ್ಯಾಯಾಧಿಪತಿಗಳು", "RUT" to "ರೂತಳು", "1SA" to "1 ಸಮುವೇಲನು", "2SA" to "2 ಸಮುವೇಲನು",
    "1KI" to "1 ಅರಸುಗಳು", "2KI" to "2 ಅರಸುಗಳು", "1CH" to "1 ಪೂರ್ವಕಾಲವೃತ್ತಾಂತ", "2CH" to "2 ಪೂರ್ವಕಾಲವೃತ್ತಾಂತ", "EZR" to "ಎಜ್ರ",
    "NEH" to "ನೆಹೆಮೀಯ", "EST" to "ಎಸ್ತೇರಳು", "JOB" to "ಯೋಬ", "PSA" to "ಕೀರ್ತನೆಗಳು", "PRO" to "ಜ್ಞಾನೋಕ್ತಿಗಳು",
    "ECC" to "ಪ್ರಸಂಗಿ", "SOL" to "ಪರಮಗೀತ", "SNG" to "ಪರಮಗೀತ", "ISA" to "ಯೆಶಾಯ", "JER" to "ಯೆರೆಮೀಯ", "LAM" to "ಪ್ರಲಾಪಗಳು",
    "EZE" to "ಯೆಹೆಜ್ಕೇಲ", "EZK" to "ಯೆಹೆಜ್ಕೇಲ", "DAN" to "ದಾನಿಯೇಲ", "HOS" to "ಹೋಶೇಯ", "JOE" to "ಯೋವೇಲ", "JOL" to "ಯೋವೇಲ", "AMO" to "ಆಮೋಸ",
    "OBA" to "ಓಬದ್ಯ", "JON" to "ಯೋನ", "MIC" to "ಮೀಕ", "NAH" to "ನಹೂಮ", "HAB" to "ಹಬಕ್ಕೂಕ",
    "ZEP" to "ಚೆಫನ್ಯ", "HAG" to "ಹಗ್ಗಾಯ", "ZEC" to "ಜೆಕರ್ಯ", "MAL" to "ಮಲಾಕಿಯ", "MAT" to "ಮತ್ತಾಯ",
    "MAR" to "ಮಾರ್ಕ", "MRK" to "ಮಾರ್ಕ", "LUK" to "ಲೂಕ", "JOH" to "ಯೋಹಾನ", "JHN" to "ಯೋಹಾನ", "ACT" to "ಅಪೊಸ್ತಲರ ಕೃತ್ಯಗಳು", "ROM" to "ರೋಮಾಪುರದವರಿಗೆ",
    "1CO" to "1 ಕೊರಿಂಥದವರಿಗೆ", "2CO" to "2 ಕೊರಿಂಥದವರಿಗೆ", "GAL" to "ಗಲಾತ್ಯದವರಿಗೆ", "EPH" to "ಎಫೆಸದವರಿಗೆ", "PHI" to "ಫಿಲಿಪ್ಪಿಯವರಿಗೆ", "PHP" to "ಫಿಲಿಪ್ಪಿಯವರಿಗೆ",
    "COL" to "ಕೊಲೊಸ್ಸೆಯವರಿಗೆ", "1TH" to "1 ಥೆಸಲೋನಿಕದವರಿಗೆ", "2TH" to "2 ಥೆಸಲೋನಿಕದವರಿಗೆ", "1TI" to "1 ತಿಮೊಥೆಯನಿಗೆ", "2TI" to "2 ತಿಮೊಥೆಯನಿಗೆ",
    "TIT" to "ತೀತನಿಗೆ", "PHM" to "ಫಿಲೆಮೋನನಿಗೆ", "HEB" to "ಇಬ್ರಿಯರಿಗೆ", "JAM" to "ಯಾಕೋಬನು", "JAS" to "ಯಾಕೋಬನು", "1PE" to "1 ಪೇತ್ರನು",
    "2PE" to "2 ಪೇತ್ರನು", "1JO" to "1 ಯೋಹಾನನು", "1JN" to "1 ಯೋಹಾನನು", "2JO" to "2 ಯೋಹಾನನು", "2JN" to "2 ಯೋಹಾನನು", "3JO" to "3 ಯೋಹಾನನು", "3JN" to "3 ಯೋಹಾನನು", "JUD" to "ಯೂದನು", "REV" to "ಪ್ರಕಟನೆ"
)

val TEL_BOOK_NAME_MAP = mapOf(
    "GEN" to "ఆదికాండము", "EXO" to "నిర్గమకాండము", "LEV" to "లేవీయకాండము", "NUM" to "సంఖ్యాకాండము", "DEU" to "ద్వితీయోపదేశకాండము",
    "JOS" to "యెహోషువ", "JDG" to "న్యాయాధిపతులు", "RUT" to "రూతు", "1SA" to "1 సమూయేలు", "2SA" to "2 సమూయేలు",
    "1KI" to "1 రాజులు", "2KI" to "2 రాజులు", "1CH" to "1 దినవృత్తాంతములు", "2CH" to "2 దినవృత్తాంతములు", "EZR" to "ఎజ్రా",
    "NEH" to "నెహెమ్యా", "EST" to "ఎస్తేరు", "JOB" to "యోబు", "PSA" to "కీర్తనలు", "PRO" to "సామెతలు",
    "ECC" to "ప్రసంగి", "SOL" to "పరమగీతము", "SNG" to "పరమగీతము", "ISA" to "యెషయా", "JER" to "యిర్మీయా", "LAM" to "విలాపవాక్యములు",
    "EZE" to "యెహెజ్కేలు", "EZK" to "యెహెజ్కేలు", "DAN" to "దానియేలు", "HOS" to "హోషేయ", "JOE" to "యోవేలు", "JOL" to "యోవేలు", "AMO" to "ఆమోసు",
    "OBA" to "ఓబద్యా", "JON" to "యోనా", "MIC" to "మీకా", "NAH" to "నహూము", "HAB" to "హబక్కూకు",
    "ZEP" to "జెఫన్యా", "HAG" to "హగ్గయి", "ZEC" to "జెకర్యా", "MAL" to "మలాకీ", "MAT" to "మత్తయి",
    "MAR" to "మార్కు", "MRK" to "మార్కు", "LUK" to "లూకా", "JOH" to "యోహాను", "JHN" to "యోహాను", "ACT" to "అపొస్తలుల కార్యములు", "ROM" to "రోమీయులకు",
    "1CO" to "1 కొరింథీయులకు", "2CO" to "2 కొరింథీయులకు", "GAL" to "గలతీయులకు", "EPH" to "ఎఫెసీయులకు", "PHI" to "ఫిలిప్పీయులకు", "PHP" to "ఫిలిప్పీయులకు",
    "COL" to "కొలొస్సయులకు", "1TH" to "1 థెస్సలొనీకయులకు", "2TH" to "2 థెస్సలొనీకయులకు", "1TI" to "1 తిమోతికి", "2TI" to "2 తిమోతికి",
    "TIT" to "తీతుకు", "PHM" to "ఫిలేమోనుకు", "HEB" to "హెబ్రీయులకు", "JAM" to "యాకోబు", "JAS" to "యాకోబు", "1PE" to "1 పేతురు",
    "2PE" to "2 పేతురు", "1JO" to "1 యోహాను", "1JN" to "1 యోహాను", "2JO" to "2 యోహాను", "2JN" to "2 యోహాను", "3JO" to "3 యోహాను", "3JN" to "3 యోహాను", "JUD" to "యూదా", "REV" to "ప్రకటన గ్రంథము"
)

val MAL_BOOK_NAME_MAP = mapOf(
    "GEN" to "ഉല്പത്തി", "EXO" to "പുറപ്പാട്", "LEV" to "ലേവ്യപുസ്തകം", "NUM" to "സംഖ്യാപുസ്തകം", "DEU" to "ആവർത്തനം",
    "JOS" to "യോശുവ", "JDG" to "ന്യായാധിപന്മാർ", "RUT" to "രൂത്ത്", "1SA" to "1 ശമൂവേൽ", "2SA" to "2 ശമൂവേൽ",
    "1KI" to "1 രാജാക്കന്മാർ", "2KI" to "2 രാജാക്കന്മാർ", "1CH" to "1 ദിനവൃത്താന്തം", "2CH" to "2 ദിനവൃത്താന്തം", "EZR" to "എസ്രാ",
    "NEH" to "നെഹെമ്യാവ്", "EST" to "എസ്ഥേർ", "JOB" to "ഇയ്യോബ്", "PSA" to "സങ്കീർത്തനങ്ങൾ", "PRO" to "സദൃശവാക്യങ്ങൾ",
    "ECC" to "സഭാപ്രസംഗി", "SOL" to "ഉത്തമഗീതം", "SNG" to "ഉത്തമഗീതം", "ISA" to "യെശയ്യാവ്", "JER" to "യിരെമ്യാവ്", "LAM" to "വിലാപങ്ങൾ",
    "EZE" to "യെഹെസ്കേൽ", "EZK" to "യെഹെസ്കേൽ", "DAN" to "ദാനീയേൽ", "HOS" to "ഹോശേയ", "JOE" to "യോവേൽ", "JOL" to "യോവേൽ", "AMO" to "ആമോസ്",
    "OBA" to "ഓബദ്യാവ്", "JON" to "യോനാ", "MIC" to "മീഖാ", "NAH" to "നഹൂം", "HAB" to "ഹബക്കൂക്",
    "ZEP" to "സെഫന്യാവ്", "HAG" to "ഹഗ്ഗായി", "ZEC" to "സെഖര്യാവ്", "MAL" to "മലാഖി", "MAT" to "മത്തായി",
    "MAR" to "മർക്കൊസ്", "MRK" to "മർക്കൊസ്", "LUK" to "ലൂക്കൊസ്", "JOH" to "യോഹന്നാൻ", "JHN" to "യോഹന്നാൻ", "ACT" to "പ്രവൃത്തികൾ", "ROM" to "റോമർ",
    "1CO" to "1 കൊരിന്ത്യർ", "2CO" to "2 കൊരിന്ത്യർ", "GAL" to "ഗലാത്യർ", "EPH" to "എഫെസ്യർ", "PHI" to "ഫിലിപ്പിയർ", "PHP" to "ഫിലിപ്പിയർ",
    "COL" to "കൊലൊസ്സ്യർ", "1TH" to "1 തെസ്സലൊനീക്യർ", "2TH" to "2 തെസ്സലൊനീക്യർ", "1TI" to "1 തിമൊഥെയൊസ്", "2TI" to "2 തിമൊഥെയൊസ്",
    "TIT" to "തീത്തോസ്", "PHM" to "ഫിലേമോൻ", "HEB" to "എബ്രായർ", "JAM" to "യാക്കോബ്", "JAS" to "യാക്കോബ്", "1PE" to "1 പത്രൊസ്",
    "2PE" to "2 പത്രൊസ്", "1JO" to "1 യോഹന്നാൻ", "1JN" to "1 യോഹന്നാൻ", "2JO" to "2 യോഹന്നാൻ", "2JN" to "2 യോഹന്നാൻ", "3JO" to "3 യോഹന്നാൻ", "3JN" to "3 യോഹന്നാൻ", "JUD" to "യൂദാ", "REV" to "വെളിപ്പാട്"
)

val TAM_BOOK_NAME_MAP = mapOf(
    "GEN" to "ஆதியாகமம்", "EXO" to "யாத்திராகமம்", "LEV" to "லேவியராகமம்", "NUM" to "எண்ணாகமம்", "DEU" to "உபாகமம்",
    "JOS" to "யோசுவா", "JDG" to "நியாயாதிபதிகள்", "RUT" to "ரூத்", "1SA" to "1 சாமுவேல்", "2SA" to "2 சாமுவேல்",
    "1KI" to "1 இராஜாக்கள்", "2KI" to "2 இராஜாக்கள்", "1CH" to "1 நாளாகமம்", "2CH" to "2 நாளாகமம்", "EZR" to "எஸ்றா",
    "NEH" to "நெகேமியா", "EST" to "எஸ்தர்", "JOB" to "யோபு", "PSA" to "சங்கீதம்", "PRO" to "நீதிமொழிகள்",
    "ECC" to "பிரசங்கி", "SOL" to "உன்னதப்பாட்டு", "SNG" to "உன்னதப்பாட்டு", "ISA" to "ஏசாயா", "JER" to "எரேமியா", "LAM" to "புலம்பல்",
    "EZE" to "எசேக்கியேல்", "EZK" to "எசேக்கியேல்", "DAN" to "தானியேல்", "HOS" to "ஓசியா", "JOE" to "யோவேல்", "JOL" to "யோவேல்", "AMO" to "ஆமோஸ்",
    "OBA" to "ஒபதியா", "JON" to "யோனா", "MIC" to "மீகா", "NAH" to "நாகூம்", "HAB" to "ஆபகூக்",
    "ZEP" to "செப்பனியா", "HAG" to "ஆகாய்", "ZEC" to "சகரியா", "MAL" to "மல்கியா", "MAT" to "மத்தேயு",
    "MAR" to "மாற்கு", "MRK" to "மாற்கு", "LUK" to "லூக்கா", "JOH" to "யோவான்", "JHN" to "யோவான்", "ACT" to "அப்போஸ்தலர்", "ROM" to "ரோமர்",
    "1CO" to "1 கொரிந்தியர்", "2CO" to "2 கொரிந்தியர்", "GAL" to "கலாத்தியர்", "EPH" to "எபேசியர்", "PHI" to "பிலிப்பியர்", "PHP" to "பிலிப்பியர்",
    "COL" to "கொலோசெயர்", "1TH" to "1 தெசலோனிக்கேயர்", "2TH" to "2 தெசலோனிக்கேயர்", "1TI" to "1 தீமோத்தேயு", "2TI" to "2 தீமோத்தேயு",
    "TIT" to "தீத்து", "PHM" to "பிலேமோன்", "HEB" to "எபிரெயர்", "JAM" to "யாக்கோபு", "JAS" to "யாக்கோபு", "1PE" to "1 பேதுரு",
    "2PE" to "2 பேதுரு", "1JO" to "1 யோவான்", "1JN" to "1 யோவான்", "2JO" to "2 யோவான்", "2JN" to "2 யோவான்", "3JO" to "3 யோவான்", "3JN" to "3 யோவான்", "JUD" to "யூதா", "REV" to "வெளிப்படுத்தின விசேஷம்"
)

val HIN_BOOK_NAME_MAP = mapOf(
    "GEN" to "उत्पत्ति", "EXO" to "निर्गमन", "LEV" to "लैव्यव्यवस्था", "NUM" to "गिनती", "DEU" to "व्यवस्थाविवरण",
    "JOS" to "यहोशू", "JDG" to "न्यायियों", "RUT" to "रूत", "1SA" to "1 शमूएल", "2SA" to "2 शमूएल",
    "1KI" to "1 राजा", "2KI" to "2 राजा", "1CH" to "1 इतिहास", "2CH" to "2 इतिहास", "EZR" to "एज्रा",
    "NEH" to "नहेमायाह", "EST" to "एस्तेर", "JOB" to "अय्यूब", "PSA" to "भजन संहिता", "PRO" to "नीतिवचन",
    "ECC" to "सभोपदेशक", "SOL" to "श्रेष्ठगीत", "SNG" to "श्रेष्ठगीत", "ISA" to "यशायाह", "JER" to "यिर्मयाह", "LAM" to "विलापगीत",
    "EZE" to "यहेजकेल", "EZK" to "यहेजकेल", "DAN" to "दानिय्येल", "HOS" to "होशे", "JOE" to "योएल", "JOL" to "योएल", "AMO" to "आमोस",
    "OBA" to "ओबद्याह", "JON" to "योना", "MIC" to "मीका", "NAH" to "नहूम", "HAB" to "हबक्कूक",
    "ZEP" to "सपन्याह", "HAG" to "हाग्गै", "ZEC" to "जकर्याह", "MAL" to "मलाकी", "MAT" to "मत्ती",
    "MAR" to "मरकुस", "MRK" to "मरकुस", "LUK" to "लूका", "JOH" to "यूहन्ना", "JHN" to "यूहन्ना", "ACT" to "प्रेरितों के काम", "ROM" to "रोमियों",
    "1CO" to "1 कुरिन्थियों", "2CO" to "2 कुरिन्थियों", "GAL" to "गलातियों", "EPH" to "इफिसियों", "PHI" to "फिलिप्पियों", "PHP" to "फिलिप्पियों",
    "COL" to "कुलुस्सियों", "1TH" to "1 थिस्सलुनीकियों", "2TH" to "2 थिस्सलुनीकियों", "1TI" to "1 तीमुथियुस", "2TI" to "2 तीमुथियुस",
    "TIT" to "तीतुस", "PHM" to "फिलेमोन", "HEB" to "इब्रानियों", "JAM" to "याकूब", "JAS" to "याकूब", "1PE" to "1 पतरस",
    "2PE" to "2 पतरस", "1JO" to "1 यूहन्ना", "1JN" to "1 यूहन्ना", "2JO" to "2 यूहन्ना", "2JN" to "2 यूहन्ना", "3JO" to "3 यूहन्ना", "3JN" to "3 यूहन्ना", "JUD" to "यहूदा", "REV" to "प्रकाशितवाक्य"
)

val SPA_BOOK_NAME_MAP = mapOf(
    "GEN" to "Génesis", "EXO" to "Éxodo", "LEV" to "Levítico", "NUM" to "Números", "DEU" to "Deuteronomio",
    "JOS" to "Josué", "JDG" to "Jueces", "RUT" to "Rut", "1SA" to "1 Samuel", "2SA" to "2 Samuel",
    "1KI" to "1 Reyes", "2KI" to "2 Reyes", "1CH" to "1 Crónicas", "2CH" to "2 Crónicas", "EZR" to "Esdras",
    "NEH" to "Nehemías", "EST" to "Ester", "JOB" to "Job", "PSA" to "Salmos", "PRO" to "Proverbios",
    "ECC" to "Eclesiastés", "SOL" to "Cantares", "SNG" to "Cantares", "ISA" to "Isaías", "JER" to "Jeremías", "LAM" to "Lamentaciones",
    "EZE" to "Ezequiel", "EZK" to "Ezequiel", "DAN" to "Daniel", "HOS" to "Oseas", "JOE" to "Joel", "JOL" to "Joel", "AMO" to "Amós",
    "OBA" to "Abdías", "JON" to "Jonás", "MIC" to "Miqueas", "NAH" to "Nahúm", "HAB" to "Habacuc",
    "ZEP" to "Sofonías", "HAG" to "Hageo", "ZEC" to "Zacarías", "MAL" to "Malaquías", "MAT" to "Mateo",
    "MAR" to "Marcos", "MRK" to "Marcos", "LUK" to "Lucas", "JOH" to "Juan", "JHN" to "Juan", "ACT" to "Hechos de los Apóstoles", "ROM" to "Romanos",
    "1CO" to "1 Corintios", "2CO" to "2 Corintios", "GAL" to "Gálatas", "EPH" to "Efesios", "PHI" to "Filipenses", "PHP" to "Filipenses",
    "COL" to "Colosenses", "1TH" to "1 Tesalonicenses", "2TH" to "2 Tesalonicenses", "1TI" to "1 Timoteo", "2TI" to "2 Timoteo",
    "TIT" to "Tito", "PHM" to "Filemón", "HEB" to "Hebreos", "JAM" to "Santiago", "JAS" to "Santiago", "1PE" to "1 Pedro",
    "2PE" to "2 Pedro", "1JO" to "1 Juan", "1JN" to "1 Juan", "2JO" to "2 Juan", "2JN" to "2 Juan", "3JO" to "3 Juan", "3JN" to "3 Juan", "JUD" to "Judas", "REV" to "Apocalipsis"
)

val HGB_BOOK_NAME_MAP = mapOf(
    "GEN" to "בְּרֵאשִׁית", "EXO" to "שְׁמוֹת", "LEV" to "וַיִּקְרָא", "NUM" to "בְּמִדְבַּר", "DEU" to "דְּבָרִים",
    "JOS" to "יְהוֹשֻׁעַ", "JDG" to "שֹׁפְטִים", "RUT" to "רוּת", "1SA" to "שְׁמוּאֵל א", "2SA" to "שְׁמוּאֵל ב",
    "1KI" to "מְלָכִים א", "2KI" to "מְלָכִים ב", "1CH" to "דִּבְרֵי הַיָּמִים א", "2CH" to "דִּבְרֵי הַיָּמִים ב",
    "EZR" to "עֶזְרָא", "NEH" to "נְחֶמְיָה", "EST" to "אֶסְתֵּר", "JOB" to "אִיּוֹב", "PSA" to "תְּהִלִּים",
    "PRO" to "מִשְׁלֵי", "ECC" to "קֹהֶלֶת", "SOL" to "שִׁיר הַשִּׁירִים", "SNG" to "שִׁיר הַשִּׁירִים", "ISA" to "יְשַׁעְיָהוּ",
    "JER" to "יִרְמְיָהוּ", "LAM" to "אֵיכָה", "EZE" to "יְחֶזְקֵאל", "EZK" to "יְחֶזְקֵאל", "DAN" to "דָּנִיֵּאל",
    "HOS" to "הוֹשֵׁעַ", "JOE" to "יוֹאֵל", "JOL" to "יוֹאֵל", "AMO" to "עָמוֹס", "OBA" to "עֹבַדְיָה", "JON" to "יוֹנָה",
    "MIC" to "מִיכָה", "NAH" to "נַחוּם", "HAB" to "חֲבַקּוּק", "ZEP" to "צְפַנְיָה", "HAG" to "חַגַּי",
    "ZEC" to "זְכַרְיָה", "MAL" to "מַלְאָכִי", "MAT" to "Κατὰ Μαθθαῖον", "MAR" to "Κατὰ Μᾶρκον", "MRK" to "Κατὰ Μᾶρκον", "LUK" to "Κατὰ Λουκᾶν",
    "JOH" to "Κατὰ Ἰωάννην", "JHN" to "Κατὰ Ἰωάννην", "ACT" to "Πράξεις Ἀποστόλων", "ROM" to "Πρὸς Ῥωμαίους", "1CO" to "Πρὸς Κορινθίους Α΄", "2CO" to "Πρὸς Κορινθίους Β΄",
    "GAL" to "Πρὸς Γαλάτας", "EPH" to "Πρὸς Ἐφεσίους", "PHI" to "Πρὸς Φιλιππησίους", "PHP" to "Πρὸς Φιλιππησίους", "COL" to "Πρὸς Κολοσσαεῖς",
    "1TH" to "Πρὸς Θεσσαλονικεῖς Α΄", "2TH" to "Πρὸς Θεσσαλονικεῖς Β΄", "1TI" to "Πρὸς Τιμόθεον Α΄", "2TI" to "Πρὸς Τιμόθεον Β΄",
    "TIT" to "Πρὸς Τίτον", "PHM" to "Πρὸς Φιλήμονα", "HEB" to "Πρὸς Ἑβραίους", "JAM" to "Ἰακώβου", "JAS" to "Ἰακώβου", "1PE" to "Πέτρου Α΄",
    "2PE" to "Πέτρου Β΄", "1JO" to "Ἰωάννου Α΄", "1JN" to "Ἰωάννου Α΄", "2JO" to "Ἰωάννου Β΄", "2JN" to "Ἰωάννου Β΄", "3JO" to "Ἰωάννου Γ΄", "3JN" to "Ἰωάννου Γ΄", "JUD" to "Ἰούδα", "REV" to "Ἀποκάλυψις Ἰωάννου"
)

fun getLocalizedBookName(bookCode: String, primaryTranslationCode: String): String {
    val code = primaryTranslationCode.uppercase()
    return when (code) {
        "KAN" -> KAN_BOOK_NAME_MAP[bookCode] ?: BOOK_NAME_MAP[bookCode] ?: bookCode
        "TEL" -> TEL_BOOK_NAME_MAP[bookCode] ?: BOOK_NAME_MAP[bookCode] ?: bookCode
        "MAL" -> MAL_BOOK_NAME_MAP[bookCode] ?: BOOK_NAME_MAP[bookCode] ?: bookCode
        "TAM" -> TAM_BOOK_NAME_MAP[bookCode] ?: BOOK_NAME_MAP[bookCode] ?: bookCode
        "HIN" -> HIN_BOOK_NAME_MAP[bookCode] ?: BOOK_NAME_MAP[bookCode] ?: bookCode
        "SPA" -> SPA_BOOK_NAME_MAP[bookCode] ?: BOOK_NAME_MAP[bookCode] ?: bookCode
        "H/G", "HGB" -> HGB_BOOK_NAME_MAP[bookCode] ?: BOOK_NAME_MAP[bookCode] ?: bookCode
        else -> BOOK_NAME_MAP[bookCode] ?: bookCode
    }
}
