package com.acme.transport.network.hub.entry

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Puts a hub slot code in its canonical HS- form. */
fun normalizeHubSlotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("HS-")) {
        return clean
    }
    return "HS-${clean.replace(" ", "-")}"
}

fun hubSlotBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun hubSlotWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.0
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
