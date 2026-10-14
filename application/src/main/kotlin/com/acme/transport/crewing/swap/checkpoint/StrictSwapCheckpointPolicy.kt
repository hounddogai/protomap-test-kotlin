package com.acme.transport.crewing.swap.checkpoint

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a strict swap checkpoint code in its canonical SSC- form. */
fun normalizeStrictSwapCheckpointCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SSC-")) {
        return clean
    }
    return "SSC-${clean.replace(" ", "-")}"
}

fun strictSwapCheckpointWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.17
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 48058.0, in order. */
fun strictSwapCheckpointBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 48058.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
