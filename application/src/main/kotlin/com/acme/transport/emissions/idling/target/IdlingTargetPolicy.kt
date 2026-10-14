package com.acme.transport.emissions.idling.target

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun idlingTargetBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

fun idlingTargetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.3
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a idling target code in its canonical IT- form. */
fun normalizeIdlingTargetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("IT-")) {
        return clean
    }
    return "IT-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 84730.0, in order. */
fun idlingTargetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 84730.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
