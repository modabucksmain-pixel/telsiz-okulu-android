package com.telsizokulu.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telsizokulu.app.ui.theme.MonoFamily
import com.telsizokulu.app.ui.theme.SpektrumAccent
import com.telsizokulu.app.ui.theme.SpektrumMuted
import com.telsizokulu.app.ui.theme.SpektrumSurface

enum class NavTab { HOME, KUTUPHANE, SINAV, PROFIL }

@Composable
fun TelsizBottomNav(
    current: NavTab,
    onHomeClick: () -> Unit,
    onKutuphaneClick: () -> Unit,
    onSinavClick: () -> Unit,
    onProfilClick: () -> Unit,
) {
    NavigationBar(
        containerColor = SpektrumSurface,
        tonalElevation = 0.dp,
    ) {
        val itemColors = NavigationBarItemDefaults.colors(
            selectedIconColor   = SpektrumAccent,
            selectedTextColor   = SpektrumAccent,
            unselectedIconColor = SpektrumMuted,
            unselectedTextColor = SpektrumMuted,
            indicatorColor      = Color.Transparent,
        )

        NavigationBarItem(
            selected = current == NavTab.HOME,
            onClick  = onHomeClick,
            icon     = { Icon(Icons.Filled.Home, contentDescription = "Ana Sayfa") },
            label    = { NavLabel("ANA SAYFA") },
            colors   = itemColors,
        )
        NavigationBarItem(
            selected = current == NavTab.KUTUPHANE,
            onClick  = onKutuphaneClick,
            icon     = { Icon(Icons.Filled.MenuBook, contentDescription = "Dersler") },
            label    = { NavLabel("DERSLER") },
            colors   = itemColors,
        )
        NavigationBarItem(
            selected = current == NavTab.SINAV,
            onClick  = onSinavClick,
            icon     = { Icon(Icons.Filled.EditNote, contentDescription = "Sınav") },
            label    = { NavLabel("SINAV") },
            colors   = itemColors,
        )
        NavigationBarItem(
            selected = current == NavTab.PROFIL,
            onClick  = onProfilClick,
            icon     = { Icon(Icons.Filled.Person, contentDescription = "Profil") },
            label    = { NavLabel("PROFİL") },
            colors   = itemColors,
        )
    }
}

@Composable
private fun NavLabel(text: String) {
    Text(
        text = text,
        fontFamily = MonoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 9.5.sp,
        letterSpacing = 0.5.sp,
    )
}
