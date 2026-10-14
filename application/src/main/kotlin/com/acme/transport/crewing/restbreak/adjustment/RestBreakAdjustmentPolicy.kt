package com.acme.transport.crewing.restbreak.adjustment

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun restBreakAdjustmentBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun restBreakAdjustmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.54
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 26929.0, in order. */
fun restBreakAdjustmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 26929.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a rest break adjustment code in its canonical RBA- form. */
fun normalizeRestBreakAdjustmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RBA-")) {
        return clean
    }
    return "RBA-${clean.replace(" ", "-")}"
}
