package com.pandacorp.knowui.presentation.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.fragula2.compose.FragulaNavHost
import com.fragula2.compose.rememberFragulaNavController
import com.fragula2.compose.swipeable
import com.pandacorp.knowui.R
import com.pandacorp.knowui.presentation.ui.screens.FactScreen
import com.pandacorp.knowui.presentation.ui.screens.LoginScreen
import com.pandacorp.knowui.presentation.ui.screens.MainScreen
import com.pandacorp.knowui.presentation.ui.screens.SettingsScreen
import com.pandacorp.knowui.presentation.ui.theme.KnowUITheme
import com.pandacorp.knowui.presentation.viewmodel.LoginViewModel
import com.pandacorp.knowui.presentation.viewmodel.PreferencesViewModel
import com.pandacorp.knowui.utils.Constants
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        val preferencesViewModel: PreferencesViewModel by viewModel()
        val loginViewModel: LoginViewModel by viewModel()

        val language = preferencesViewModel.getLanguage()
        preferencesViewModel.setLanguage(language)
        super.onCreate(savedInstanceState)

        setTheme(R.style.AppCompat) // Set an AppCompat theme before calling setContent, because SplashTheme doesn't inherit it
        setContent {
            val themeState by preferencesViewModel.themeLiveData.observeAsState(preferencesViewModel.getTheme())
            val theme = themeState.ifEmpty { Constants.Preferences.THEME_DEFAULT }

            KnowUITheme(theme = theme) {
                MainActivityContent(isSigned = loginViewModel.isSigned)
            }
        }
    }
}

@Composable
private fun MainActivityContent(isSigned: Boolean = true,) {
    val navController = rememberFragulaNavController()

    // viewModelStoreOwner to provide shared viewmodels for composable functions
    val viewModelStoreOwner =
        checkNotNull(LocalViewModelStoreOwner.current) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        AnimatedContent(
            targetState = isSigned,
            label = "LoginScreenTransition",
            transitionSpec = {
                slideInHorizontally(
                    animationSpec = tween(500, easing = LinearEasing),
                    initialOffsetX = { it },
                ) togetherWith
                    slideOutHorizontally(animationSpec = tween(500, easing = LinearEasing))
            },
        ) {
            when (it) {
                true -> {
                    FragulaNavHost(
                        navController = navController,
                        startDestination = Constants.Screen.MAIN,
                    ) {
                        swipeable(Constants.Screen.MAIN) {
                            ProvideViewModelStoreOwnerContent(viewModelStoreOwner) {
                                MainScreen(navController = navController)
                            }
                        }

                        swipeable(Constants.Screen.SETTINGS) {
                            ProvideViewModelStoreOwnerContent(viewModelStoreOwner) {
                                SettingsScreen(navController = navController)
                            }
                        }

                        swipeable(Constants.Screen.FACT) {
                            ProvideViewModelStoreOwnerContent(viewModelStoreOwner) {
                                FactScreen(navController = navController)
                            }
                        }
                    }
                }

                false -> {
                    ProvideViewModelStoreOwnerContent(viewModelStoreOwner) {
                        LoginScreen()
                    }
                }
            }
        }
    }
}

@Composable
private fun ProvideViewModelStoreOwnerContent(
    viewModelStoreOwner: ViewModelStoreOwner,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
        content()
    }
}