package com.example.autodroid.feature.weather.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.autodroid.feature.weather.domain.model.RouteWeatherForecast
import com.example.autodroid.feature.weather.domain.model.RouteWeatherPoint
import com.example.autodroid.feature.weather.ui.util.weatherCodeIcon

/**
 * Displays a vertical timeline of weather conditions along the active navigation route.
 * Each waypoint shows its ETA offset, weather icon, and temperature.
 */
@Composable
fun RouteWeatherTimeline(
    forecast: RouteWeatherForecast,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Route Weather",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        forecast.points.forEachIndexed { index, point ->
            val label = resolveWaypointLabel(index, forecast)
            RouteWeatherPointCard(point = point, label = label)
        }
    }
}

/** Picks a human-readable label: origin, destination, or intermediate index. */
private fun resolveWaypointLabel(index: Int, forecast: RouteWeatherForecast): String {
    if (index == 0) return forecast.origin.name
    if (index == forecast.points.lastIndex) return forecast.destination.name
    return "Waypoint ${index + 1}"
}

/** A single weather card for one point along the route. */
@Composable
private fun RouteWeatherPointCard(point: RouteWeatherPoint, label: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = weatherCodeIcon(point.forecast.weatherCode),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = label, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "ETA: +${point.etaMinutes} min",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${point.forecast.maxTemp.toInt()}°C",
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}
