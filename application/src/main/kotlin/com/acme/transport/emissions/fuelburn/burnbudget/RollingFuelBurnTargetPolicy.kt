package com.acme.transport.emissions.fuelburn.burnbudget

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Puts a rolling fuel burn target code in its canonical RFBT- form. */
fun normalizeRollingFuelBurnTargetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RFBT-")) {
        return clean
    }
    return "RFBT-${clean.replace(" ", "-")}"
}

fun rollingFuelBurnTargetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.8
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun rollingFuelBurnTargetBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
