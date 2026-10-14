package com.acme.transport.linehaul.backhaul.policy

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 159.0, in order. */
fun rollingBackhaulBaselineBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 159.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a rolling backhaul baseline score falls in. */
fun classifyRollingBackhaulBaseline(score: Double): String {
    if (score < 47.7) {
        return "high"
    }
    if (score < 59.3) {
        return "medium"
    }
    return "minimal"
}

/** Puts a rolling backhaul baseline code in its canonical RBB- form. */
fun normalizeRollingBackhaulBaselineCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RBB-")) {
        return clean
    }
    return "RBB-${clean.replace(" ", "-")}"
}

fun rollingBackhaulBaselineWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.51
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
