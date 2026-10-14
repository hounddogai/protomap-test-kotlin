package com.acme.transport.terminal.crossdock.rollup

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun tieredCrossDockClaimWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.42
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun tieredCrossDockClaimBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Puts a tiered cross dock claim code in its canonical TCDC- form. */
fun normalizeTieredCrossDockClaimCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TCDC-")) {
        return clean
    }
    return "TCDC-${clean.replace(" ", "-")}"
}
