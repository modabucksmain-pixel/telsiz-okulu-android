package com.telsizokulu.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.telsizokulu.app.data.repository.CurriculumRepository
import com.telsizokulu.app.data.repository.ProgressRepository
import com.telsizokulu.app.engine.AudioEngine
import com.telsizokulu.app.ui.screens.*
import com.telsizokulu.app.ui.viewmodel.*

@Composable
fun AppNavGraph(audioEngine: AudioEngine) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val curriculumRepo = remember { CurriculumRepository(context) }
    val progressRepo = remember { ProgressRepository(context) }

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {

        // ── Ana Sayfa ──────────────────────────────────────────
        composable(Screen.Home.route) {
            val vm: HomeViewModel = viewModel(
                factory = HomeViewModelFactory(curriculumRepo, progressRepo)
            )
            HomeScreen(
                viewModel = vm,
                onDersClick = { bolumId, altBolumId, dersNo ->
                    navController.navigate(Screen.Ders.createRoute(bolumId, altBolumId, dersNo))
                },
                onBolumClick = { bolumId ->
                    navController.navigate(Screen.Bolum.createRoute(bolumId))
                },
                onSinavMenuClick = {
                    navController.navigate(Screen.SinavMenu.route)
                },
                onProfilClick = {
                    navController.navigate(Screen.Profil.route)
                },
                onKutuphaneClick = {
                    navController.navigate(Screen.Kutuphane.route)
                },
                onPratikClick = {
                    navController.navigate(Screen.Pratik.route)
                }
            )
        }

        // ── Bölüm Detay ───────────────────────────────────────
        composable(
            route = Screen.Bolum.route,
            arguments = listOf(navArgument("bolumId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bolumId = backStackEntry.arguments?.getString("bolumId") ?: ""
            val vm: BolumViewModel = viewModel(
                factory = BolumViewModelFactory(curriculumRepo, progressRepo, bolumId)
            )
            BolumScreen(
                viewModel = vm,
                onGeri = { navController.popBackStack() },
                onDersClick = { altBolumId, dersNo ->
                    navController.navigate(Screen.Ders.createRoute(bolumId, altBolumId, dersNo))
                },
                onAltBolumSinavClick = { altBolumId ->
                    navController.navigate(Screen.Sinav.createRoute("alt_bolum", bolumId, altBolumId))
                },
                onBolumSinavClick = {
                    navController.navigate(Screen.Sinav.createRoute("bolum", bolumId))
                }
            )
        }

        // ── Ders / Egzersiz ───────────────────────────────────
        composable(
            route = Screen.Ders.route,
            arguments = listOf(
                navArgument("bolumId")    { type = NavType.StringType },
                navArgument("altBolumId") { type = NavType.StringType },
                navArgument("dersNo")     { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val bolumId    = backStackEntry.arguments?.getString("bolumId")    ?: ""
            val altBolumId = backStackEntry.arguments?.getString("altBolumId") ?: ""
            val dersNo     = backStackEntry.arguments?.getInt("dersNo")        ?: 1
            val vm: ExerciseViewModel = viewModel(
                factory = ExerciseViewModelFactory(
                    curriculumRepo, progressRepo, audioEngine,
                    bolumId, altBolumId, dersNo, "ders"
                )
            )
            ExerciseScreen(
                viewModel = vm,
                onGeri = { navController.popBackStack() },
                onTamamlandi = { navController.popBackStack() }
            )
        }

        // ── Sınav ──────────────────────────────────────────────
        composable(
            route = Screen.Sinav.route,
            arguments = listOf(
                navArgument("sinavTipi")  { type = NavType.StringType },
                navArgument("bolumId")    { type = NavType.StringType; defaultValue = "" },
                navArgument("altBolumId") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val sinavTipi  = backStackEntry.arguments?.getString("sinavTipi")  ?: "genel"
            val bolumId    = backStackEntry.arguments?.getString("bolumId")    ?: ""
            val altBolumId = backStackEntry.arguments?.getString("altBolumId") ?: ""
            val vm: ExerciseViewModel = viewModel(
                factory = ExerciseViewModelFactory(
                    curriculumRepo, progressRepo, audioEngine,
                    bolumId, altBolumId, 0, sinavTipi
                )
            )
            ExerciseScreen(
                viewModel = vm,
                onGeri = { navController.popBackStack() },
                onTamamlandi = { navController.popBackStack() }
            )
        }

        // ── Sınav Menüsü ────────────────────────────────────
        composable(Screen.SinavMenu.route) {
            SinavMenuScreen(
                onGeri = { navController.popBackStack() },
                onGenelSinavClick = {
                    navController.navigate(Screen.Sinav.createRoute("genel"))
                }
            )
        }

        // ── Profil ────────────────────────────────────────────
        composable(Screen.Profil.route) {
            val vm: ProfileViewModel = viewModel(
                factory = ProfileViewModelFactory(curriculumRepo, progressRepo)
            )
            ProfileScreen(
                viewModel = vm,
                onGeri = { navController.popBackStack() }
            )
        }

        // ── Kütüphane ─────────────────────────────────────────
        composable(Screen.Kutuphane.route) {
            KutuphaneScreen(
                curriculumRepo = curriculumRepo,
                progressRepo = progressRepo,
                onGeri = { navController.popBackStack() }
            )
        }

        // ── Pratik ─────────────────────────────────────────────
        composable(Screen.Pratik.route) {
            val vm: ExerciseViewModel = viewModel(
                factory = ExerciseViewModelFactory(
                    curriculumRepo, progressRepo, audioEngine,
                    "pratik", "pratik", 0, "pratik"
                )
            )
            ExerciseScreen(
                viewModel = vm,
                onGeri = { navController.popBackStack() },
                onTamamlandi = { navController.popBackStack() }
            )
        }
    }
}
