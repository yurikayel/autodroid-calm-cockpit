package com.example.autodroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.autodroid.core.automotive.DrivingStateManager
import com.example.autodroid.core.automotive.SettingsManager
import com.example.autodroid.core.designsystem.AutoDroidTheme
import com.example.autodroid.core.designsystem.AutoIcons
import com.example.autodroid.core.designsystem.LocalCarDimens
import com.example.autodroid.core.designsystem.isWideLayout
import com.example.autodroid.feature.dashboard.DashboardRoute
import com.example.autodroid.feature.radio.RadioDetailRoute
import com.example.autodroid.feature.radio.RadioRoute
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
data object DashboardDestination

@Serializable
data object RadioDestination

@Serializable
data class RadioDetailDestination(val id: String)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsManager: SettingsManager

    @Inject
    lateinit var drivingStateManager: DrivingStateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val currentBrand by settingsManager.currentBrand.collectAsStateWithLifecycle()
            val isDriving by drivingStateManager.isDriving.collectAsStateWithLifecycle()

            AutoDroidTheme(brand = currentBrand, focusMode = isDriving) {
                MainAppNavigation(onCycleBrand = settingsManager::cycleBrand)
            }
        }
    }
}

@Composable
fun MainAppNavigation(onCycleBrand: () -> Unit) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    val onRadioTab = destination?.hierarchy?.any {
        it.hasRoute(RadioDestination::class) || it.hasRoute(RadioDetailDestination::class)
    } == true
    val onDashboard = destination?.hasRoute(DashboardDestination::class) == true

    // Car-first: rail when wide; bottom bar on phone.
    val layoutType = if (isWideLayout()) {
        NavigationSuiteType.NavigationRail
    } else {
        NavigationSuiteType.NavigationBar
    }
    val dimens = LocalCarDimens.current

    NavigationSuiteScaffold(
        layoutType = layoutType,
        navigationSuiteItems = {
            item(
                selected = onDashboard,
                onClick = { navController.navigateToTab(DashboardDestination) },
                icon = {
                    Icon(
                        AutoIcons.Home,
                        contentDescription = "Drive",
                        modifier = Modifier.size(dimens.iconPrimary)
                    )
                },
                label = { Text("Drive") }
            )
            item(
                selected = onRadioTab,
                onClick = { navController.navigateToTab(RadioDestination) },
                icon = {
                    Icon(
                        AutoIcons.Radio,
                        contentDescription = "Radio",
                        modifier = Modifier.size(dimens.iconPrimary)
                    )
                },
                label = { Text("Radio") }
            )
            item(
                selected = false,
                onClick = onCycleBrand,
                icon = {
                    Icon(
                        AutoIcons.Palette,
                        contentDescription = "Switch brand theme",
                        modifier = Modifier.size(dimens.iconPrimary)
                    )
                },
                label = { Text("Theme") }
            )
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = DashboardDestination,
            modifier = Modifier.fillMaxSize()
        ) {
            composable<DashboardDestination> {
                DashboardRoute()
            }
            composable<RadioDestination> {
                RadioRoute(
                    onStationClick = { id -> navController.navigate(RadioDetailDestination(id)) }
                )
            }
            composable<RadioDetailDestination> { entry ->
                RadioDetailRoute(
                    stationId = entry.toRoute<RadioDetailDestination>().id,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

private fun NavHostController.navigateToTab(route: Any) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
