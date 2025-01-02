package com.example.jetpackcompose.api

import android.util.Log
import com.example.jetpackcompose.data.ForecastData
import com.example.jetpackcompose.data.WeatherData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// Objekt für den Zugriff auf den Weather API Service
object WeatherApiService {

    // Die Basis-URL der OpenWeatherMap API
    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"

    // OkHttpClient wird zum Verwalten der HTTP-Anfragen verwendet
    private val client = OkHttpClient.Builder().build()

    // Retrofit-Instanz für den API-Zugriff erstellen
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)  // Setzt die Basis-URL für alle Anfragen
        .client(client)     // Verwendet den OkHttpClient
        .addConverterFactory(GsonConverterFactory.create())  // Gson Converter für JSON-Daten
        .build()

    // Erstellen der API-Instanz, um die Anfragen zu definieren
    private val api = retrofit.create(WeatherApi::class.java)

    // Schnittstelle für die API-Aufrufe
    interface WeatherApi {
        // Funktion, um aktuelle Wetterdaten abzurufen
        @GET("weather")
        suspend fun fetchWeather(
            @Query("q") city: String,           // Stadtname
            @Query("appid") apiKey: String,     // API-Schlüssel
            @Query("units") units: String = "metric"  // Einheiten für die Temperatur (default: Celsius)
        ): retrofit2.Response<WeatherData>  // Rückgabewert: Wetterdaten (WeatherData)

        // Funktion, um Wettervorhersagedaten abzurufen
        @GET("forecast")
        suspend fun fetchForecast(
            @Query("q") city: String,           // Stadtname
            @Query("appid") apiKey: String,     // API-Schlüssel
            @Query("units") units: String = "metric"  // Einheiten für die Temperatur (default: Celsius)
        ): retrofit2.Response<ForecastData>  // Rückgabewert: Vorhersagedaten (ForecastData)
    }

    // Funktion, um das aktuelle Wetter für eine Stadt abzurufen
    suspend fun fetchWeather(city: String, apiKey: String): WeatherData? {
        return try {
            // Wechselt den Dispatcher, um die API-Abfrage im Hintergrund auszuführen
            withContext(Dispatchers.Default) {
                // Führt die API-Anfrage aus
                val response = api.fetchWeather(city, apiKey)
                // Überprüft, ob die Antwort erfolgreich war
                if (response.isSuccessful) {
                    response.body()  // Gibt die Wetterdaten zurück, wenn erfolgreich
                } else {
                    // Loggt eine Fehlermeldung, wenn die Antwort fehlschlägt
                    Log.e("WeatherApiService", "Failed to fetch data: ${response.code()}")
                    null  // Gibt null zurück, wenn ein Fehler auftritt
                }
            }
        } catch (e: Exception) {
            // Loggt eine Fehlermeldung, wenn während des Abrufs ein Fehler auftritt
            Log.e("WeatherApiService", "Error fetching data: ${e.message}")
            null  // Gibt null zurück bei Ausnahmefehlern
        }
    }

    // Funktion, um die Wettervorhersage für eine Stadt abzurufen
    suspend fun fetchForecast(city: String, apiKey: String): ForecastData? {
        return try {
            // Wechselt den Dispatcher, um die API-Abfrage im Hintergrund auszuführen
            withContext(Dispatchers.IO) {
                // Führt die API-Anfrage aus
                val response = api.fetchForecast(city, apiKey)
                // Überprüft, ob die Antwort erfolgreich war
                if (response.isSuccessful) {
                    response.body()  // Gibt die Vorhersagedaten zurück, wenn erfolgreich
                } else {
                    // Loggt eine Fehlermeldung, wenn die Antwort fehlschlägt
                    Log.e("WeatherApiService", "Failed to fetch forecast data: ${response.code()} ${response.message()}")
                    null  // Gibt null zurück, wenn ein Fehler auftritt
                }
            }
        } catch (e: Exception) {
            // Loggt eine Fehlermeldung, wenn während des Abrufs ein Fehler auftritt
            Log.e("WeatherApiService", "Error fetching forecast data: ${e.message}")
            null  // Gibt null zurück bei Ausnahmefehlern
        }
    }
}
