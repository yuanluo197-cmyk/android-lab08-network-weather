package com.luoyuan.lab08

import android.app.Activity
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson

class WeatherActivity : Activity() {
    private lateinit var queue: RequestQueue
    private lateinit var status: TextView
    private lateinit var body: LinearLayout
    private var active: Request<*>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val code = intent.getStringExtra(CODE)
        val city = intent.getStringExtra(NAME).orEmpty()
        if (code == null || code.length != 9 || !code.all(Char::isDigit)) {
            finish()
            return
        }
        val root = screen()
        root.addView(label("$city · 天气详情", 23f, true))
        val actions = LinearLayout(this)
        actions.addView(action("返回城市列表") { finish() }, LinearLayout.LayoutParams(0, dp(48), 1f))
        actions.addView(action("刷新天气") { load(code) }, LinearLayout.LayoutParams(0, dp(48), 1f))
        root.addView(actions)
        status = label("")
        root.addView(status)
        body = column()
        root.addView(ScrollView(this).apply { addView(body) }, LinearLayout.LayoutParams(-1, 0, 1f))
        queue = Volley.newRequestQueue(applicationContext)
        load(code)
    }

    private fun load(code: String) {
        active?.cancel()
        status.text = "正在请求实时天气…"
        body.removeAllViews()
        val request = JsonObjectRequest(Request.Method.GET, "$BASE/$code", null, { json ->
            runCatching {
                val response = Gson().fromJson(json.toString(), WeatherResponse::class.java)
                val data = response.data
                if (response.status != 200 || data == null || data.wendu.isNullOrBlank()) {
                    status.text = "该城市暂无可用天气，可返回选择其他城市"
                } else {
                    showWeather(response, data)
                }
            }.onFailure {
                status.text = "天气数据格式异常，请刷新重试"
            }
        }, {
            status.text = "天气请求失败，请检查网络后点击刷新"
        })
        request.retryPolicy = DefaultRetryPolicy(6500, 1, 1f)
        request.setShouldCache(false)
        active = queue.add(request)
    }

    private fun showWeather(response: WeatherResponse, data: WeatherData) {
        status.text = "预报服务：ITBoy   ·   ${response.time ?: response.cityInfo?.updateTime ?: "更新时间未知"}"
        val today = data.forecast?.firstOrNull()
        val summary = LinearLayout(this).apply {
            setBackgroundColor(0xFFDFEDF6.toInt())
            setPadding(dp(12), dp(10), dp(12), dp(10))
            gravity = android.view.Gravity.CENTER_VERTICAL
        }
        val big = label("${data.wendu} ℃   ${today?.type.orEmpty()}", 32f, true)
        summary.addView(big, LinearLayout.LayoutParams(0, -2, 1f))
        if (!today?.type.isNullOrBlank()) {
            summary.addView(ImageView(this).apply {
                setImageResource(icon(today!!.type.orEmpty()))
                contentDescription = today.type
            }, LinearLayout.LayoutParams(dp(58), dp(58)))
        }
        body.addView(summary)
        body.addView(label("${today?.low ?: "低温 —"}  /  ${today?.high ?: "高温 —"}", 18f, true))
        body.addView(label("湿度 ${data.shidu ?: "—"}     空气质量 ${data.quality ?: "—"}"))
        body.addView(label("PM2.5 ${data.pm25 ?: "—"}     PM10 ${data.pm10 ?: "—"}"))
        if (today != null) {
            body.addView(label("${today.fx.orEmpty()} ${today.fl.orEmpty()}    日出 ${today.sunrise ?: "—"}    日落 ${today.sunset ?: "—"}"))
        }
        data.ganmao?.takeIf { it.isNotBlank() }?.let { body.addView(label(it)) }
        if (!data.forecast.isNullOrEmpty()) {
            body.addView(label("未来天气", 19f, true))
            data.forecast.drop(1).take(7).forEach { day ->
                body.addView(label("${day.ymd.orEmpty()}  ${day.week.orEmpty()}  ${day.type.orEmpty()}\n${day.low.orEmpty()} / ${day.high.orEmpty()}  ${day.fx.orEmpty()}${day.fl.orEmpty()}"))
            }
        }
    }

    private fun icon(type: String): Int = when {
        "雷" in type -> R.drawable.thunder
        "雪" in type -> R.drawable.snow
        "雨" in type -> R.drawable.rain
        "阴" in type -> R.drawable.cloud
        "云" in type -> R.drawable.mcloud
        "晴" in type -> R.drawable.sun
        else -> R.drawable.cloud
    }

    override fun onDestroy() {
        active?.cancel()
        if (::queue.isInitialized) queue.stop()
        super.onDestroy()
    }

    companion object {
        const val CODE = "city_code"
        const val NAME = "city_name"
        private const val BASE = "http://t.weather.itboy.net/api/weather/city"
    }
}
