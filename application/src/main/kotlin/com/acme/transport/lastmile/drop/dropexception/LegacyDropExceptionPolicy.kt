package com.acme.transport.lastmile.drop.dropexception

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Puts a legacy drop exception code in its canonical LDE- form. */
fun normalizeLegacyDropExceptionCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LDE-")) {
        return clean
    }
    return "LDE-${clean.replace(" ", "-")}"
}

fun legacyDropExceptionBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun legacyDropExceptionWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.92
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
