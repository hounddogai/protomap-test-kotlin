package com.acme.transport.lastmile.parcel.cycle

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun tieredParcelSummaryBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Puts a tiered parcel summary code in its canonical TPS- form. */
fun normalizeTieredParcelSummaryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TPS-")) {
        return clean
    }
    return "TPS-${clean.replace(" ", "-")}"
}

fun tieredParcelSummaryWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.33
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
