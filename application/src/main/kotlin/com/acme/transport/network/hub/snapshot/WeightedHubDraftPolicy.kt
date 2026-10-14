package com.acme.transport.network.hub.snapshot

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a weighted hub draft code in its canonical WHD- form. */
fun normalizeWeightedHubDraftCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WHD-")) {
        return clean
    }
    return "WHD-${clean.replace(" ", "-")}"
}

fun weightedHubDraftSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

fun weightedHubDraftWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.44
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 49085.0, in order. */
fun weightedHubDraftBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 49085.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
