package com.acme.transport.terminal.traileryard.yardforecast

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun seasonalTrailerYardScoreBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Counts how many amounts fit in a budget of 56488.0, in order. */
fun seasonalTrailerYardScoreBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 56488.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a seasonal trailer yard score code in its canonical STYS- form. */
fun normalizeSeasonalTrailerYardScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("STYS-")) {
        return clean
    }
    return "STYS-${clean.replace(" ", "-")}"
}

fun seasonalTrailerYardScoreWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.22
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
