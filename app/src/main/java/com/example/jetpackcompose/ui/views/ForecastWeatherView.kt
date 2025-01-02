package com.example.jetpackcompose.ui.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.jetpackcompose.data.ForecastItem
import com.example.jetpackcompose.storage.Keys
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.jetpackcompose.viewmodel.WeatherViewModel
import com.example.jetpackcompose.ui.components.SearchBarSample
import com.example.jetpackcompose.ui.components.WeatherCard

// Composable-Funktion, die die Wettervorhersage anzeigt
@Composable
fun ForecastWeatherView(forecast: List<ForecastItem>) {
    // Zugriff auf den aktuellen Kontext (z.B. für DataStore)
    val context = LocalContext.current

    // Zustände für die Speicherung von Heimatstadt und API-Schlüssel
    var hometown by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf("") }

    // Zugriff auf das ViewModel für die Wetterdaten
    val weatherViewModel: WeatherViewModel = viewModel()

    // Zustandsbeobachtung für Fehlermeldungen
    val errorMessage by weatherViewModel.errorMessage.collectAsState()

    // LaunchedEffect wird beim ersten Aufruf der Composable-Funktion ausgeführt
    // Hier werden die Werte von Heimatstadt und API-Schlüssel aus dem DataStore abgerufen
    LaunchedEffect(Unit) {
        context.dataStore.data.collect { preferences ->
            // Heimatstadt und API-Schlüssel aus dem DataStore holen
            hometown = preferences[Keys.HOMETOWN_KEY] ?: ""
            apiKey = preferences[Keys.API_TOKEN_KEY] ?: ""

            // Wenn Heimatstadt und API-Schlüssel gesetzt sind, wird die Wettervorhersage abgerufen
            if (hometown.isNotEmpty() && apiKey.isNotEmpty()) {
                weatherViewModel.fetchForecastData(hometown, apiKey)
            }
        }
    }

    // Zustand für die Suchabfrage der Stadt
    val searchQuery = rememberSaveable { mutableStateOf("") }

    // Box, um die Suchleiste zu platzieren
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Die Suchleiste für die Vorhersage wird angezeigt
        SearchBarSample(
            selectedMenu = "Forecast", // Titel für die Suchleiste
            apiKey = apiKey,  // API-Schlüssel
            onQueryChanged = { query ->
                // Suchabfrage ändern
                searchQuery.value = query
                // Wenn etwas eingegeben wurde, wird die Vorhersage für diese Stadt abgerufen
                if (query.isNotEmpty()) {
                    weatherViewModel.fetchForecastData(query, apiKey)
                } else {
                    // Wenn keine Eingabe vorhanden ist, wird die Vorhersage für die Heimatstadt abgerufen
                    if (hometown.isNotEmpty() && apiKey.isNotEmpty()) {
                        weatherViewModel.fetchForecastData(hometown, apiKey)
                    }
                }
            }
        )
    }

    // Fehlernachricht anzeigen, falls vorhanden
    errorMessage?.let {
        Text(
            text = it,
            color = Color.Red,  // Rote Farbe für die Fehlermeldung
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 25.sp),
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)  // Zentriert den Text
        )
    }

    // Layout für den gesamten Bildschirm
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(24.dp)) // Abstand oben

        // Überprüft, ob die Suchabfrage und die Heimatstadt leer sind
        if (searchQuery.value.isEmpty() && hometown.isEmpty()) {
            // Wenn keine Stadt angegeben ist, wird eine Aufforderung angezeigt
            Text(
                text = "Set your hometown in settings",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 24.sp,
                    color = Color.Gray
                ),
                modifier = Modifier.padding(16.dp)
            )
        } else if (forecast.isNotEmpty()) {
            // Wenn Vorhersagedaten vorhanden sind, wird der Titel mit der Stadt angezeigt
            Text(
                text = "Forecast for ${searchQuery.value.takeIf { it.isNotEmpty() } ?: hometown}",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 28.sp,
                    color = Color.Black
                ),
                modifier = Modifier
                    .padding(bottom = 32.dp)
                    .align(Alignment.CenterHorizontally) // Zentriert den Titel
            )

            // LazyColumn zum Anzeigen der Wettervorhersage
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                // Jedes Vorhersageelement wird als "WeatherCard" angezeigt
                items(forecast) { forecastItem ->
                    WeatherCard(forecastItem = forecastItem)  // Anzeige der Wetterkarte für jedes Element
                    Spacer(modifier = Modifier.height(8.dp))  // Abstand zwischen den Karten
                }
            }
        }
    }
}
