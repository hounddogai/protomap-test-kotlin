package com.acme.transport.fleetops.tire.tier

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a scheduled tire score code in its canonical STS- form. */
fun normalizeScheduledTireScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("STS-")) {
        return clean
    }
    return "STS-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 84093.0, in order. */
fun scheduledTireScoreBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 84093.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun scheduledTireScoreWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.37
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun scheduledTireScoreSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}
