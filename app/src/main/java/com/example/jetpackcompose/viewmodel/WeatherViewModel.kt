package com.example.jetpackcompose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpackcompose.api.WeatherApiService
import com.example.jetpackcompose.data.ForecastItem
import com.example.jetpackcompose.data.WeatherData
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// ViewModel-Klasse für das Abrufen und Verwalten von Wetterdaten und Vorhersagen
class WeatherViewModel : ViewModel() {

    // StateFlow für das aktuelle Wetter (initial null)
    private val _currentWeather = MutableStateFlow<WeatherData?>(null)
    // Öffentliche unveränderliche Referenz für das aktuelle Wetter
    val currentWeather: StateFlow<WeatherData?> = _currentWeather

    // StateFlow für die Wettervorhersage (initial eine leere Liste)
    private val _forecast = MutableStateFlow<List<ForecastItem>>(emptyList())
    // Öffentliche unveränderliche Referenz für die Wettervorhersage
    val forecast: StateFlow<List<ForecastItem>> = _forecast

    // StateFlow für die URL des Wettericons (initial null)
    private val _iconUrl = MutableStateFlow<String?>(null)
    // Öffentliche unveränderliche Referenz für das Icon-URL
    val iconUrl: StateFlow<String?> get() = _iconUrl

    // StateFlow für die Fehlermeldung (initial null)
    private val _errorMessage = MutableStateFlow<String?>(null)
    // Öffentliche unveränderliche Referenz für die Fehlermeldung
    val errorMessage: StateFlow<String?> get() = _errorMessage

    // Funktion zum Abrufen der aktuellen Wetterdaten
    fun fetchWeatherData(city: String, apiKey: String) {
        // Startet eine Coroutine im ViewModel-Scope
        viewModelScope.launch {
            try {
                // API-Aufruf für das aktuelle Wetter
                val weatherResponse = WeatherApiService.fetchWeather(city, apiKey)
                if (weatherResponse != null) {
                    // Wenn das Wetter erfolgreich abgerufen wurde, wird es im StateFlow gespeichert
                    _currentWeather.value = weatherResponse
                    // Holt das Wettericon basierend auf dem Icon-Code
                    fetchWeatherIcon(weatherResponse.weather.firstOrNull()?.icon.orEmpty())
                    // Setzt die Fehlermeldung auf null, da der Abruf erfolgreich war
                    _errorMessage.value = null
                } else {
                    // Setzt eine Fehlermeldung, wenn keine Daten abgerufen wurden
                    _errorMessage.value = "Failed to fetch weather. Please check your API key or city name."
                }
            } catch (e: Exception) {
                // Fehlerbehandlung: Setzt eine Fehlermeldung im Falle eines Fehlers
                _errorMessage.value = "An error occurred: ${e.localizedMessage}"
            }
        }
    }

    // Funktion zum Abrufen der Wettervorhersagedaten
    fun fetchForecastData(city: String, apiKey: String) {
        // Startet eine Coroutine im ViewModel-Scope
        viewModelScope.launch {
            try {
                // API-Aufruf für die Vorhersagedaten
                val forecastResponse = WeatherApiService.fetchForecast(city, apiKey)
                if (forecastResponse != null) {
                    // Wenn die Vorhersage erfolgreich abgerufen wurde, wird sie im StateFlow gespeichert
                    _forecast.value = forecastResponse.list
                    // Setzt die Fehlermeldung auf null, da der Abruf erfolgreich war
                    _errorMessage.value = null
                } else {
                    // Setzt eine Fehlermeldung, wenn keine Vorhersagedaten abgerufen wurden
                    _errorMessage.value = "Failed to fetch forecast. Please check your API key or city name."
                }
            } catch (e: Exception) {
                // Fehlerbehandlung: Setzt eine Fehlermeldung im Falle eines Fehlers
                _errorMessage.value = "An error occurred while fetching forecast: ${e.localizedMessage}"
            }
        }
    }

    // Funktion, um das Wettericon basierend auf dem Icon-Code zu generieren
    private fun fetchWeatherIcon(iconId: String) {
        // Wenn das Icon-Id nicht leer ist, wird die URL des Icons gesetzt
        if (iconId.isNotEmpty()) {
            _iconUrl.value = "https://openweathermap.org/img/wn/$iconId@2x.png"
        }
    }
}
