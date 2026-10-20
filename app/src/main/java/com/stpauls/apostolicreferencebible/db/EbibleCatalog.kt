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
    EbibleCatalogItem("swhonen", "SWH", "Swahili", "Tanzania / Kenya", "Biblia Habari Njema", "https://ebible.org/Scriptures/swhonen_vpl.zip"),
    EbibleCatalogItem("fraLSG", "FRA", "French", "France / Worldwide", "Louis Segond 1910", "https://ebible.org/Scriptures/fraLSG_vpl.zip"),
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
    EbibleCatalogItem("urd", "URD", "Urdu", "Pakistan / India", "Urdu Geo Version", "https://ebible.org/Scriptures/urd_vpl.zip"),
    EbibleCatalogItem("ukr1996", "UKR", "Ukrainian", "Ukraine", "Ukrainian Kulish Bible", "https://ebible.org/Scriptures/ukr1996_vpl.zip"),
    EbibleCatalogItem("hausa", "HAU", "Hausa", "Nigeria / West Africa", "Hausa Bible", "https://ebible.org/Scriptures/hausa_vpl.zip"),
    EbibleCatalogItem("yor", "YOR", "Yoruba", "Nigeria / Benin", "Yoruba Bible", "https://ebible.org/Scriptures/yor_vpl.zip"),
    EbibleCatalogItem("ibo", "IBO", "Igbo", "Nigeria", "Igbo Union Bible", "https://ebible.org/Scriptures/ibo_vpl.zip"),
    EbibleCatalogItem("cmn2006", "CUV", "Chinese", "China / Worldwide", "Chinese Union Version 2006", "https://ebible.org/Scriptures/cmn2006_vpl.zip")
)
