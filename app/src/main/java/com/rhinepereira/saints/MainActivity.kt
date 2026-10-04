package com.rhinepereira.saints

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.google.firebase.messaging.FirebaseMessaging
import com.rhinepereira.saints.navigation.NavGraph
import com.rhinepereira.saints.ui.theme.AngelsAndSaintsTheme
import java.util.Locale

class MainActivity : ComponentActivity() {

    private var currentIntent by mutableStateOf<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        currentIntent = intent

        FirebaseMessaging.getInstance().subscribeToTopic("daily_readings")
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) Log.w("FCM", "Topic subscription failed", task.exception)
            }

        setContent {
            val language by app.language.value.collectAsState()
            val locale = remember(language) { Locale.forLanguageTag(language.tag) }
            val baseConfig = LocalConfiguration.current
            val config = remember(locale, baseConfig) {
                Configuration(baseConfig).apply { setLocale(locale) }
            }
            val localized = remember(config) { createConfigurationContext(config) }

            // In-app language switch: string resources resolve through this context.
            CompositionLocalProvider(
                LocalContext provides localized,
                LocalConfiguration provides config,
                // These default to looking the owner up from LocalContext, which is no longer the Activity.
                LocalActivity provides this,
                LocalActivityResultRegistryOwner provides this,
                LocalOnBackPressedDispatcherOwner provides this
            ) {
                AngelsAndSaintsTheme {
                    val navController = rememberNavController()
                    LaunchedEffect(currentIntent) {
                        currentIntent?.data?.let { uri ->
                            navController.navigate(uri) { launchSingleTop = true }
                            currentIntent?.data = null
                        } ?: run {
                            if (currentIntent?.getStringExtra("NAVIGATE_TO") == "daily-readings") {
                                navController.navigate("today") { launchSingleTop = true }
                                currentIntent?.removeExtra("NAVIGATE_TO")
                            }
                        }
                    }
                    NavGraph(navController, language, locale)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        currentIntent = intent
    }
}
