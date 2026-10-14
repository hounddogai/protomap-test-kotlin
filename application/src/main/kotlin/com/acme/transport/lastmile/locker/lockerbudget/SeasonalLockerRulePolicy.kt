package com.acme.transport.lastmile.locker.lockerbudget

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a seasonal locker rule code in its canonical SLR- form. */
fun normalizeSeasonalLockerRuleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SLR-")) {
        return clean
    }
    return "SLR-${clean.replace(" ", "-")}"
}

fun seasonalLockerRuleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.56
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
