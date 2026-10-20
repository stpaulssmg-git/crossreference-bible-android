package com.stpauls.apostolicreferencebible.db

data class EbibleCatalogItem(
    val id: String,
    val code: String,
    val language: String,
    val country: String,
    val title: String,
    val vplUrl: String
)

val EBIBLE_CATALOG = listOf(
    // --- Indian Languages ---
    EbibleCatalogItem("hin2017", "HIN", "Hindi", "India", "Indian Revised Version (Hindi)", "https://ebible.org/Scriptures/hin2017_vpl.zip"),
    EbibleCatalogItem("benirv", "BEN", "Bengali", "India / Bangladesh", "Indian Revised Version (Bengali)", "https://ebible.org/Scriptures/benirv_vpl.zip"),
    EbibleCatalogItem("tel2017", "TEL", "Telugu", "India (Andhra Pradesh / Telangana)", "Indian Revised Version (Telugu)", "https://ebible.org/Scriptures/tel2017_vpl.zip"),
    EbibleCatalogItem("tam2017", "TAM", "Tamil", "India (Tamil Nadu) / Sri Lanka", "Indian Revised Version (Tamil)", "https://ebible.org/Scriptures/tam2017_vpl.zip"),
    EbibleCatalogItem("kanirv", "KAN", "Kannada", "India (Karnataka)", "Indian Revised Version (Kannada)", "https://ebible.org/Scriptures/kanirv_vpl.zip"),
    EbibleCatalogItem("mal", "MAL", "Malayalam", "India (Kerala)", "Malayalam VPL Bible", "https://ebible.org/Scriptures/mal_vpl.zip"),
    EbibleCatalogItem("mar", "MAR", "Marathi", "India (Maharashtra)", "Marathi Bible", "https://ebible.org/Scriptures/mar_vpl.zip"),
    EbibleCatalogItem("guj2017", "GUJ", "Gujarati", "India (Gujarat)", "Indian Revised Version (Gujarati)", "https://ebible.org/Scriptures/guj2017_vpl.zip"),
    EbibleCatalogItem("asmfb", "ASM", "Assamese", "India (Assam)", "Free Bible Assamese", "https://ebible.org/Scriptures/asmfb_vpl.zip"),
    EbibleCatalogItem("ory", "ORY", "Odia (Oriya)", "India (Odisha)", "Odia Bible", "https://ebible.org/Scriptures/ory_vpl.zip"),
    EbibleCatalogItem("pan", "PAN", "Punjabi", "India (Punjab) / Pakistan", "Punjabi Bible", "https://ebible.org/Scriptures/pan_vpl.zip"),
    EbibleCatalogItem("mai", "MAI", "Maithili", "India (Bihar) / Nepal", "Maithili Bible", "https://ebible.org/Scriptures/mai_vpl.zip"),
    EbibleCatalogItem("mni", "MNI", "Manipuri (Meitei)", "India (Manipur)", "Manipuri Bible", "https://ebible.org/Scriptures/mni_vpl.zip"),
    EbibleCatalogItem("nag", "NAG", "Nagamese", "India (Nagaland)", "Nagamese Bible", "https://ebible.org/Scriptures/nag_vpl.zip"),
    EbibleCatalogItem("sandev", "SAN", "Sanskrit", "India (Devanagari)", "Sanskrit Holy Bible", "https://ebible.org/Scriptures/sandev_vpl.zip"),
    EbibleCatalogItem("urdgvr", "URD", "Urdu", "India / Pakistan", "Urdu Geo Version", "https://ebible.org/Scriptures/urdgvr_vpl.zip"),

    // --- Major International World Languages ---
    EbibleCatalogItem("swhonen", "SWH", "Swahili", "Tanzania / Kenya / East Africa", "Biblia Habari Njema", "https://ebible.org/Scriptures/swhonen_vpl.zip"),
    EbibleCatalogItem("fraLSG", "FRA", "French", "France / Africa / Worldwide", "Louis Segond 1910", "https://ebible.org/Scriptures/fraLSG_vpl.zip"),
    EbibleCatalogItem("spaRV1909", "SPA", "Spanish", "Spain / Latin America", "Reina Valera 1909", "https://ebible.org/Scriptures/spaRV1909_vpl.zip"),
    EbibleCatalogItem("amh", "AMH", "Amharic", "Ethiopia", "Amharic Holy Bible", "https://ebible.org/Scriptures/amh_vpl.zip"),
    EbibleCatalogItem("arb-vd", "ARB", "Arabic", "Middle East / Worldwide", "Van Dyck Arabic Bible", "https://ebible.org/Scriptures/arb-vd_vpl.zip"),
    EbibleCatalogItem("tglulb", "TGL", "Tagalog", "Philippines", "Unlocked Literal Bible", "https://ebible.org/Scriptures/tglulb_vpl.zip"),
    EbibleCatalogItem("deu1912", "DEU", "German", "Germany / Europe", "Luther Bibel 1912", "https://ebible.org/Scriptures/deu1912_vpl.zip"),
    EbibleCatalogItem("porbr2018", "POR", "Portuguese", "Brazil / Portugal", "Bíblia Livre 2018", "https://ebible.org/Scriptures/porbr2018_vpl.zip"),
    EbibleCatalogItem("russyn", "RUS", "Russian", "Russia / Eastern Europe", "Russian Synodal Bible", "https://ebible.org/Scriptures/russyn_vpl.zip"),
    EbibleCatalogItem("kor", "KOR", "Korean", "Korea", "Korean Bible", "https://ebible.org/Scriptures/kor_vpl.zip"),
    EbibleCatalogItem("jpnm", "JPN", "Japanese", "Japan", "Japanese Meiji Bible", "https://ebible.org/Scriptures/jpnm_vpl.zip"),
    EbibleCatalogItem("vie1934", "VIE", "Vietnamese", "Vietnam", "Vietnamese Cadman Bible", "https://ebible.org/Scriptures/vie1934_vpl.zip"),
    EbibleCatalogItem("ukr1996", "UKR", "Ukrainian", "Ukraine", "Ukrainian Kulish Bible", "https://ebible.org/Scriptures/ukr1996_vpl.zip"),
    EbibleCatalogItem("hausa", "HAU", "Hausa", "Nigeria / West Africa", "Hausa Bible", "https://ebible.org/Scriptures/hausa_vpl.zip"),
    EbibleCatalogItem("yor", "YOR", "Yoruba", "Nigeria / Benin", "Yoruba Bible", "https://ebible.org/Scriptures/yor_vpl.zip"),
    EbibleCatalogItem("ibo", "IBO", "Igbo", "Nigeria", "Igbo Union Bible", "https://ebible.org/Scriptures/ibo_vpl.zip"),
    EbibleCatalogItem("cmn2006", "CUV", "Chinese", "China / Worldwide", "Chinese Union Version 2006", "https://ebible.org/Scriptures/cmn2006_vpl.zip")
).sortedWith(compareBy({ it.language.lowercase() }, { it.title.lowercase() }))
