package com.acme.transport.lastmile.parcel.adaptiveparcelmapping

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Puts a scheduled parcel mapping code in its canonical SPM- form. */
fun normalizeScheduledParcelMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SPM-")) {
        return clean
    }
    return "SPM-${clean.replace(" ", "-")}"
}

fun scheduledParcelMappingSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

fun scheduledParcelMappingWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.9
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun scheduledParcelMappingBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
