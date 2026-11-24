package com.luoyuan.lab08

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

internal fun Activity.dp(value: Int) = (value * resources.displayMetrics.density).toInt()
internal fun Activity.label(value: String, size: Float = 16f, bold: Boolean = false) =
    TextView(this).apply {
        text = value
        textSize = size
        setTextColor(Color.rgb(22, 55, 74))
        if (bold) setTypeface(null, Typeface.BOLD)
        setPadding(0, dp(5), 0, dp(5))
    }
internal fun Activity.action(value: String, onTap: () -> Unit) = Button(this).apply {
    text = value
    isAllCaps = false
    setOnClickListener { onTap() }
}
internal fun Activity.column() = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
internal fun Activity.screen(): LinearLayout = column().also { root ->
    root.layoutParams = ViewGroup.LayoutParams(-1, -1)
    root.setBackgroundColor(Color.rgb(242, 247, 251))
    root.setOnApplyWindowInsetsListener { view, insets ->
        view.setPadding(dp(16) + insets.systemWindowInsetLeft, dp(8) + insets.systemWindowInsetTop,
            dp(16) + insets.systemWindowInsetRight, dp(8) + insets.systemWindowInsetBottom)
        insets
    }
    setContentView(root)
    root.requestApplyInsets()
    root.addView(label(getString(R.string.app_name), 24f, true))
}
