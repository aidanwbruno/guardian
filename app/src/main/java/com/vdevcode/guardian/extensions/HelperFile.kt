package com.vdevcode.guardian.extensions

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.util.Log
import android.view.PointerIcon
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.vdevcode.guardian.helpers.Guardian
import kotlinx.coroutines.*

/**
 * My Extensions Functions
 */

fun View.bg(color: Int) = this.setBackgroundColor(ActivityCompat.getColor(context, color))

fun MaterialCardView.cardBg(color: Int) = this.setCardBackgroundColor(ActivityCompat.getColor(context, color))

fun View.color(color: Int) = this.setBackgroundColor(ActivityCompat.getColor(context, color))
fun View.mhide() {
    this.visibility = View.GONE
}

fun View.mshow() {
    this.visibility = View.VISIBLE
}

fun MaterialButton.newIcon(icon: Int) {
    this.icon = ActivityCompat.getDrawable(context, icon)
}


// hide keyword
fun View?.hideKeyword() = this?.let { v ->
    val imm = v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    imm?.hideSoftInputFromWindow(v.windowToken, 0)
}

fun TextInputLayout.ok(et: TextInputEditText, field: String): Boolean {
    if (et.text.toString().isBlank()) {
        this.error = "O campo $field  é obrigatório"
        return false
    }
    return true
}
