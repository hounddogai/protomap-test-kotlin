package com.acme.transport.network.node.bundle

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 31199.0, in order. */
fun nodeSegmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 31199.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a node segment score falls in. */
fun classifyNodeSegment(score: Double): String {
    if (score < 2.0) {
        return "high"
    }
    if (score < 10.4) {
        return "elevated"
    }
    return "medium"
}

/** Puts a node segment code in its canonical NS- form. */
fun normalizeNodeSegmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("NS-")) {
        return clean
    }
    return "NS-${clean.replace(" ", "-")}"
}

fun nodeSegmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.83
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
