package com.example.autodroid.feature.radio

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.autodroid.BuildConfig
import com.example.autodroid.core.demo.DemoScenario
import com.example.autodroid.core.designsystem.AmbientBackground
import com.example.autodroid.core.designsystem.AutoIcons
import com.example.autodroid.core.designsystem.LocalBrand
import com.example.autodroid.core.designsystem.LocalCarDimens
import com.example.autodroid.core.designsystem.LocalFocusMode
import com.example.autodroid.core.designsystem.isWideLayout
import com.example.autodroid.domain.model.RadioStation
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RadioRoute(
    onStationClick: (String) -> Unit,
    viewModel: RadioViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val demoScenario by viewModel.demoScenario.collectAsStateWithLifecycle()
    val wide = isWideLayout()
    var showDemoSheet by remember { mutableStateOf(false) }
    val focusMode = LocalFocusMode.current

    AmbientBackground(brand = LocalBrand.current, focusMode = focusMode) {
        if (wide) {
            val navigator = rememberListDetailPaneScaffoldNavigator<String>()
            val scope = rememberCoroutineScope()
            BackHandler(enabled = navigator.canNavigateBack()) {
                scope.launch { navigator.navigateBack() }
            }
            ListDetailPaneScaffold(
                directive = navigator.scaffoldDirective,
                value = navigator.scaffoldValue,
                listPane = {
                    AnimatedPane {
                        RadioListPane(
                            uiState = uiState,
                            onRetry = viewModel::loadStations,
                            onStationClick = { id ->
                                scope.launch {
                                    navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, id)
                                }
                            },
                            onOpenDemo = { showDemoSheet = true }
                        )
                    }
                },
                detailPane = {
                    AnimatedPane {
                        val id = navigator.currentDestination?.contentKey
                        if (id != null) {
                            RadioDetailRoute(
                                stationId = id,
                                onBack = { scope.launch { navigator.navigateBack() } }
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Select a station",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            )
        } else {
            RadioListPane(
                uiState = uiState,
                onRetry = viewModel::loadStations,
                onStationClick = onStationClick,
                onOpenDemo = { showDemoSheet = true }
            )
        }
    }

    if (showDemoSheet && BuildConfig.DEBUG) {
        ModalBottomSheet(
            onDismissRequest = { showDemoSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Demo scenario", style = MaterialTheme.typography.headlineSmall)
                DemoScenario.entries.forEach { scenario ->
                    FilterChip(
                        selected = scenario == demoScenario,
                        onClick = {
                            viewModel.selectDemoScenario(scenario)
                            showDemoSheet = false
                        },
                        label = {
                            Text(scenario.name.lowercase().replaceFirstChar { it.uppercase() })
                        }
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun RadioListPane(
    uiState: RadioUiState,
    onRetry: () -> Unit,
    onStationClick: (String) -> Unit,
    onOpenDemo: () -> Unit
) {
    val dimens = LocalCarDimens.current
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.contentPadding, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Stations", style = MaterialTheme.typography.headlineMedium)
            if (BuildConfig.DEBUG) {
                IconButton(
                    onClick = onOpenDemo,
                    modifier = Modifier.size(dimens.touchTarget)
                ) {
                    Icon(AutoIcons.Tune, contentDescription = "Demo controls")
                }
            }
        }
        RadioScreen(
            uiState = uiState,
            onRetry = onRetry,
            onStationClick = onStationClick
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RadioScreen(
    uiState: RadioUiState,
    onRetry: () -> Unit,
    onStationClick: (String) -> Unit
) {
    val dimens = LocalCarDimens.current
    Box(modifier = Modifier.fillMaxSize()) {
        when (uiState) {
            RadioUiState.Loading -> {
                LoadingIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(64.dp)
                )
            }

            RadioUiState.Empty -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GenreAvatar(genre = "Empty", modifier = Modifier.size(96.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No stations available.", style = MaterialTheme.typography.titleLarge)
                }
            }

            is RadioUiState.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = uiState.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onRetry,
                        modifier = Modifier.height(dimens.touchTarget)
                    ) {
                        Text("Retry")
                    }
                }
            }

            is RadioUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(dimens.contentPadding),
                    verticalArrangement = Arrangement.spacedBy(dimens.targetSpacing)
                ) {
                    items(uiState.stations, key = { it.id }) { station ->
                        StationRow(station = station, onClick = { onStationClick(station.id) })
                    }
                    if (uiState.hiddenCount > 0) {
                        item { RestrictedFooter(hiddenCount = uiState.hiddenCount) }
                    }
                }
            }
        }
    }
}

@Composable
fun StationRow(station: RadioStation, onClick: () -> Unit) {
    val dimens = LocalCarDimens.current
    val compact = !isWideLayout()
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(dimens.listRowHeight)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(dimens.tileRadius),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (compact) 12.dp else 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GenreAvatar(
                genre = station.genre,
                modifier = Modifier.size(dimens.avatarSize),
                compact = compact
            )
            Spacer(modifier = Modifier.width(if (compact) 12.dp else 20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = station.name,
                    style = if (compact) {
                        MaterialTheme.typography.titleMedium
                    } else {
                        MaterialTheme.typography.titleLarge
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${station.frequency} · ${station.genre}",
                    style = if (compact) {
                        MaterialTheme.typography.bodyMedium
                    } else {
                        MaterialTheme.typography.bodyLarge
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun StationCard(station: RadioStation, onClick: () -> Unit) {
    StationRow(station, onClick)
}

@Composable
private fun RestrictedFooter(hiddenCount: Int) {
    val dimens = LocalCarDimens.current
    val compact = !isWideLayout()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(0.55f)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            AutoIcons.Lock,
            contentDescription = null,
            modifier = Modifier.size(dimens.iconSecondary)
        )
        Text(
            text = "$hiddenCount more when parked",
            style = if (compact) {
                MaterialTheme.typography.bodyMedium
            } else {
                MaterialTheme.typography.titleMedium
            }
        )
    }
}

@Composable
fun GenreAvatar(
    genre: String,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val color = when (genre.lowercase()) {
        "rock" -> MaterialTheme.colorScheme.secondary
        "jazz" -> MaterialTheme.colorScheme.tertiary
        "news", "traffic" -> MaterialTheme.colorScheme.primary
        "classical" -> MaterialTheme.colorScheme.primaryContainer
        "electronic" -> MaterialTheme.colorScheme.tertiaryContainer
        "country", "pop" -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val shape = when (genre.lowercase()) {
        "rock" -> RoundedCornerShape(12.dp)
        "jazz" -> RoundedCornerShape(28.dp)
        "news", "traffic" -> RoundedCornerShape(8.dp)
        "classical" -> RoundedCornerShape(50)
        else -> RoundedCornerShape(20.dp)
    }
    Box(
        modifier = modifier.background(color, shape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = genre.take(1).uppercase(),
            style = if (compact) {
                MaterialTheme.typography.titleMedium
            } else {
                MaterialTheme.typography.titleLarge
            },
            color = Color.White
        )
    }
}
