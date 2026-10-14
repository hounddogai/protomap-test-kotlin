package com.acme.transport.crewing.restbreak.regionalbreakpreference

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun fallbackRestBreakAssignmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.88
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun fallbackRestBreakAssignmentBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Counts how many amounts fit in a budget of 5758.0, in order. */
fun fallbackRestBreakAssignmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 5758.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a fallback rest break assignment code in its canonical FRBA- form. */
fun normalizeFallbackRestBreakAssignmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FRBA-")) {
        return clean
    }
    return "FRBA-${clean.replace(" ", "-")}"
}
