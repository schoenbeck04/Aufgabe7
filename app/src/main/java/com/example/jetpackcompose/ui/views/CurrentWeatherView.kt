package com.example.jetpackcompose.ui.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.jetpackcompose.data.WeatherData
import com.example.jetpackcompose.storage.Keys
import androidx.compose.ui.platform.LocalContext
import com.example.jetpackcompose.viewmodel.WeatherViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.jetpackcompose.ui.components.SearchBarSample

// Composable-Funktion, die aktuelle Wetterdaten anzeigt
@Composable
fun CurrentWeatherView(currentWeather: WeatherData?, iconUrl: String?) {

    // Zugriff auf das ViewModel für aktuelle Wetterdaten
    val weatherViewModel: WeatherViewModel = viewModel()
    // Zustandsbeobachtung der aktuellen Wetterdaten und des Icons
    val currentWeather by weatherViewModel.currentWeather.collectAsState()
    val iconUrl by weatherViewModel.iconUrl.collectAsState()

    // Variablen für Heimatstadt und API-Schlüssel
    var hometown by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf("") }
    // Zustandsbeobachtung für Fehlermeldungen
    val errorMessage by weatherViewModel.errorMessage.collectAsState()

    // Zugriff auf den aktuellen Kontext, z.B. für DataStore
    val context = LocalContext.current

    // LaunchedEffect: Wird einmalig ausgeführt, um Heimatstadt und API-Schlüssel aus DataStore zu laden
    LaunchedEffect(Unit) {
        context.dataStore.data.collect { preferences ->
            hometown = preferences[Keys.HOMETOWN_KEY] ?: ""
            apiKey = preferences[Keys.API_TOKEN_KEY] ?: ""
            // Wenn Heimatstadt gesetzt ist, wird das Wetter abgerufen
            if (hometown.isNotEmpty()) {
                weatherViewModel.fetchWeatherData(hometown, apiKey)
            }
        }
    }

    // Zustand für die Suchabfrage der Stadt
    val searchQuery = rememberSaveable { mutableStateOf("") }

    // Box, um die Suchleiste für die Stadt abzufragen
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        SearchBarSample(
            selectedMenu = "Home", // Titel für die Suchleiste
            apiKey = apiKey,  // API-Schlüssel
            onQueryChanged = { query ->
                searchQuery.value = query
                // Wenn der Benutzer eine Stadt eingibt, wird das Wetter für diese Stadt abgerufen
                if (query.isNotEmpty()) {
                    weatherViewModel.fetchWeatherData(query, apiKey)
                }
            }
        )
    }

    // Anzeige der Fehlermeldung, falls vorhanden
    errorMessage?.let {
        Text(
            text = it,
            color = Color.Red,  // Rote Farbe für die Fehlermeldung
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 25.sp),
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally) // Zentriert den Text
        )
    }

    // Wenn eine Suchabfrage oder Heimatstadt gesetzt ist, wird das aktuelle Wetter angezeigt
    if (searchQuery.value.isNotEmpty() || hometown.isNotEmpty()) {
        currentWeather?.let {
            // Layout für das aktuelle Wetter
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(Color(0xFFBBDEFB), RoundedCornerShape(32.dp)) // Hintergrundfarbe und abgerundete Ecken
                    .padding(16.dp)
            ) {
                // Zeile für Stadtname und Wetter-Icon
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        // Anzeige des Städtenamens und des Landes
                        Text(
                            text = "${it.name}, ${it.sys.country}",
                            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 30.sp),
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    // Anzeige des Wetter-Icons
                    iconUrl?.let {
                        Image(
                            painter = rememberAsyncImagePainter(it),
                            contentDescription = "Weather icon",
                            modifier = Modifier.size(120.dp)
                        )
                    }
                }

                // Funktion zum Erstellen einer Zeile mit Wetterinformationen
                @Composable
                fun createWeatherInfoRow(label: String, value: String) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                                .padding(start = 32.dp)
                        ) {
                            // Beschriftung der Wetterinformation
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 22.sp),
                                color = Color.Gray,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(start = 45.dp)
                        ) {
                            // Wert der Wetterinformation
                            Text(
                                text = value,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 22.sp),
                                color = Color.Gray,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Anzeige der verschiedenen Wetterdaten
                createWeatherInfoRow("Description:", it.weather[0].description)  // Beschreibung
                Spacer(modifier = Modifier.height(8.dp))
                createWeatherInfoRow("Temp.:", "${it.main.temp}°C")  // Temperatur
                Spacer(modifier = Modifier.height(8.dp))
                createWeatherInfoRow("Feels Like:", "${it.main.feels_like}°C")  // Gefühlte Temperatur
                Spacer(modifier = Modifier.height(8.dp))
                createWeatherInfoRow("Humidity:", "${it.main.humidity}%")  // Luftfeuchtigkeit
                Spacer(modifier = Modifier.height(8.dp))
                createWeatherInfoRow("Wind:", "${it.wind.speed} m/s")  // Windgeschwindigkeit
                Spacer(modifier = Modifier.height(8.dp))

                // Umwandlung des Unix-Zeitstempels in eine Uhrzeit
                val sunriseTime = convertUnixToTime(it.sys.sunrise)
                val sunsetTime = convertUnixToTime(it.sys.sunset)

                // Anzeige der Sonnenaufgangs- und Sonnenuntergangszeiten
                createWeatherInfoRow("Sunrise:", sunriseTime)
                Spacer(modifier = Modifier.height(4.dp))
                createWeatherInfoRow("Sunset:", sunsetTime)
            }
        } ?: Text(
            text = "No current weather data available.",  // Falls keine Wetterdaten vorhanden sind
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
            color = Color.Gray,
            modifier = Modifier.padding(16.dp)
        )
    } else {
        // Wenn weder eine Suchabfrage noch eine Heimatstadt gesetzt sind
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Set your hometown in settings",  // Aufforderung, die Heimatstadt in den Einstellungen zu setzen
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 24.sp),
            color = Color.Gray,
            modifier = Modifier.padding(16.dp)
        )
    }
}

// Hilfsfunktion zur Umwandlung eines Unix-Zeitstempels in eine Uhrzeit
fun convertUnixToTime(timestamp: Long): String {
    val date = Date(timestamp * 1000)  // Unix-Timestamp wird in Millisekunden umgerechnet
    val format = SimpleDateFormat("HH:mm", Locale.getDefault())  // Format für Uhrzeit (Stunden:Minuten)
    return format.format(date)  // Rückgabe der formatierten Uhrzeit
}
