package com.acme.transport.network.hub.target

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a hub claim code in its canonical HC- form. */
fun normalizeHubClaimCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("HC-")) {
        return clean
    }
    return "HC-${clean.replace(" ", "-")}"
}

fun hubClaimWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.48
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
