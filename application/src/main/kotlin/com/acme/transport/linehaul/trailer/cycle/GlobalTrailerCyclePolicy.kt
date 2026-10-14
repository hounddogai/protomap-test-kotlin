package com.acme.transport.linehaul.trailer.cycle

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Counts how many amounts fit in a budget of 19116.0, in order. */
fun globalTrailerCycleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 19116.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun globalTrailerCycleBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Puts a global trailer cycle code in its canonical GTC- form. */
fun normalizeGlobalTrailerCycleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GTC-")) {
        return clean
    }
    return "GTC-${clean.replace(" ", "-")}"
}

fun globalTrailerCycleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.02
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
