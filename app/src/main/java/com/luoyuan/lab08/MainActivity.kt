package com.luoyuan.lab08

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import com.google.gson.Gson
import java.util.concurrent.Executors

class MainActivity : Activity() {
    private val io = Executors.newSingleThreadExecutor()
    private data class Entry(val name: String, val code: String, val path: String) {
        override fun toString() = if (path.isBlank()) "$name   ·   $code" else "$name   ·   $path   ·   $code"
    }

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        val root = screen()
        root.addView(label("城市天气", 20f, true))
        val search = EditText(this).apply { hint = "输入城市名、地区或城市编码"; isSingleLine = true }
        root.addView(search)
        val status = label("正在读取本地城市列表…")
        root.addView(status)
        val list = ListView(this)
        val adapter = ArrayAdapter<Entry>(this, android.R.layout.simple_list_item_1, ArrayList())
        list.adapter = adapter
        root.addView(list, LinearLayout.LayoutParams(-1, 0, 1f))
        var cities = emptyList<Entry>()

        fun filter(query: String) {
            val key = query.trim()
            val matches = if (key.isEmpty()) cities else cities.filter {
                it.name.contains(key, true) || it.code.contains(key) || it.path.contains(key, true)
            }
            adapter.clear()
            adapter.addAll(matches)
            status.text = "城市列表  ·  ${matches.size} 个"
        }

        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { filter(s.toString()) }
            override fun afterTextChanged(value: Editable?) {}
        })
        list.setOnItemClickListener { _, _, position, _ ->
            val city = adapter.getItem(position) ?: return@setOnItemClickListener
            startActivity(Intent(this, WeatherActivity::class.java)
                .putExtra(WeatherActivity.CODE, city.code).putExtra(WeatherActivity.NAME, city.name))
        }
        io.execute {
            val result = runCatching {
                val rows = assets.open("citycode.json").bufferedReader(Charsets.UTF_8).use {
                    Gson().fromJson(it, Array<City>::class.java).toList()
                }
                val byId = rows.associateBy { it.id }
                rows.mapNotNull { city ->
                    val code = city.code?.takeIf { it.length == 9 && it.all(Char::isDigit) }
                        ?: return@mapNotNull null
                    val name = city.name?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                    val parent = byId[city.pid]
                    val path = listOfNotNull(parent?.let { byId[it.pid]?.name }, parent?.name)
                        .distinct().joinToString(" / ")
                    Entry(name, code, path)
                }.distinctBy { it.code }
            }
            runOnUiThread {
                if (isDestroyed) return@runOnUiThread
                result.onSuccess { cities = it; filter(search.text.toString()) }
                    .onFailure { status.text = "城市文件读取失败" }
            }
        }
    }

    override fun onDestroy() {
        io.shutdownNow()
        super.onDestroy()
    }
}
