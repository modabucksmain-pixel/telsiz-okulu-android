package com.telsizokulu.app.ui.navigation

/**
 * Uygulama ekranlarının tüm route tanımları
 */
sealed class Screen(val route: String) {
    object Home     : Screen("home")
    object Bolum    : Screen("bolum/{bolumId}") {
        fun createRoute(bolumId: String) = "bolum/$bolumId"
    }
    object Ders     : Screen("ders/{bolumId}/{altBolumId}/{dersNo}") {
        fun createRoute(bolumId: String, altBolumId: String, dersNo: Int) =
            "ders/$bolumId/$altBolumId/$dersNo"
    }
    object Sinav    : Screen("sinav/{sinavTipi}?bolumId={bolumId}&altBolumId={altBolumId}") {
        fun createRoute(
            sinavTipi: String,   // "alt_bolum" | "bolum" | "genel"
            bolumId: String = "",
            altBolumId: String = ""
        ) = "sinav/$sinavTipi?bolumId=$bolumId&altBolumId=$altBolumId"
    }
    object SinavMenu: Screen("sinav_menu")
    object Profil   : Screen("profil")
    object Kutuphane: Screen("kutuphane")
    object Pratik   : Screen("pratik")
}
