package com.luoyuan.lab08

import com.google.gson.annotations.SerializedName

data class City(
    val id: Int,
    val pid: Int,
    @SerializedName("city_code") val code: String?,
    @SerializedName("city_name") val name: String?
)

data class WeatherResponse(val status: Int, val message: String?, val time: String?,
    val cityInfo: CityInfo?, val data: WeatherData?)
data class CityInfo(val city: String?, val updateTime: String?)
data class WeatherData(val wendu: String?, val shidu: String?, val pm25: Double?, val pm10: Double?,
    val quality: String?, val ganmao: String?, val forecast: List<Forecast>?)
data class Forecast(val ymd: String?, val week: String?, val high: String?, val low: String?,
    val type: String?, val fx: String?, val fl: String?, val sunrise: String?, val sunset: String?,
    val aqi: Int?, val notice: String?)
