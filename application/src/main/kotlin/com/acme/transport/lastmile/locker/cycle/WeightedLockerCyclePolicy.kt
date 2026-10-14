package com.acme.transport.lastmile.locker.cycle

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun weightedLockerCycleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.04
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a weighted locker cycle code in its canonical WLC- form. */
fun normalizeWeightedLockerCycleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WLC-")) {
        return clean
    }
    return "WLC-${clean.replace(" ", "-")}"
}
