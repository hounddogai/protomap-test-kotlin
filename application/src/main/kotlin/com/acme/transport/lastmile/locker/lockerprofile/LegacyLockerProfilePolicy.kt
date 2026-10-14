package com.acme.transport.lastmile.locker.lockerprofile

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a legacy locker profile code in its canonical LLP- form. */
fun normalizeLegacyLockerProfileCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LLP-")) {
        return clean
    }
    return "LLP-${clean.replace(" ", "-")}"
}

fun legacyLockerProfileWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.2
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
