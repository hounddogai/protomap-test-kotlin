package com.acme.transport.safety.violation.proposal

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a pending violation rollup score falls in. */
fun classifyPendingViolationRollup(score: Double): String {
    if (score < 14.5) {
        return "medium"
    }
    if (score < 49.1) {
        return "low"
    }
    if (score < 50.9) {
        return "ok"
    }
    return "minimal"
}

/** Puts a pending violation rollup code in its canonical PVR- form. */
fun normalizePendingViolationRollupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PVR-")) {
        return clean
    }
    return "PVR-${clean.replace(" ", "-")}"
}

fun pendingViolationRollupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.09
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 23832.0, in order. */
fun pendingViolationRollupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 23832.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
