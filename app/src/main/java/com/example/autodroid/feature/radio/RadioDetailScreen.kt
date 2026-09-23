package com.example.autodroid.feature.radio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.autodroid.core.designsystem.AutoIcons
import com.example.autodroid.core.designsystem.LocalCarDimens
import com.example.autodroid.core.designsystem.isWideLayout

@Composable
fun RadioDetailRoute(
    stationId: String,
    onBack: () -> Unit,
    viewModel: RadioDetailViewModel = hiltViewModel<RadioDetailViewModel, RadioDetailViewModel.Factory>(
        key = stationId,
        creationCallback = { factory -> factory.create(stationId) }
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    RadioDetailScreen(uiState = uiState, onRetry = viewModel::load, onBack = onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadioDetailScreen(
    uiState: RadioDetailUiState,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    val dimens = LocalCarDimens.current
    val showTopBar = !isWideLayout()
    val title = when (uiState) {
        is RadioDetailUiState.Success -> uiState.station.name
        else -> "Station"
    }

    Scaffold(
        topBar = {
            if (showTopBar) {
                TopAppBar(
                    title = {
                        Text(
                            text = title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(dimens.touchTarget)
                        ) {
                            Icon(AutoIcons.ArrowBack, contentDescription = "Back to stations")
                        }
                    }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(dimens.contentPadding)
        ) {
            when (uiState) {
                RadioDetailUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                RadioDetailUiState.NotFound -> Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Station not found.", style = MaterialTheme.typography.titleLarge)
                    if (!showTopBar) BackButton(onBack)
                }

                is RadioDetailUiState.Error -> Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        uiState.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Button(
                        onClick = onRetry,
                        modifier = Modifier.height(dimens.touchTarget)
                    ) {
                        Text("Retry")
                    }
                    if (!showTopBar) BackButton(onBack)
                }

                is RadioDetailUiState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        GenreAvatar(
                            genre = uiState.station.genre,
                            modifier = Modifier.size(if (showTopBar) 96.dp else 120.dp),
                            compact = showTopBar
                        )
                        Text(
                            text = uiState.station.name,
                            style = MaterialTheme.typography.headlineLarge,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            uiState.station.frequency,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            uiState.station.genre,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "Now playing",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        if (!showTopBar) {
                            Spacer(modifier = Modifier.height(8.dp))
                            BackButton(onBack)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BackButton(onBack: () -> Unit) {
    Button(
        onClick = onBack,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
    ) {
        Text("Back to stations", color = MaterialTheme.colorScheme.onSecondary)
    }
}
