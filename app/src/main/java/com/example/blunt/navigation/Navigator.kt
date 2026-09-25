package com.example.blunt.navigation

import androidx.compose.runtime.staticCompositionLocalOf

class Navigator(val go: (String) -> Unit, val back: () -> Unit, val message: (String) -> Unit)
val LocalNavigator = staticCompositionLocalOf<Navigator> { error("Navigator not provided") }
