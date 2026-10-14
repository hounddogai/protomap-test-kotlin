package com.acme.transport.freightpay.audit.cycle

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a fallback audit target score falls in. */
fun classifyFallbackAuditTarget(score: Double): String {
    if (score < 17.6) {
        return "ok"
    }
    if (score < 66.0) {
        return "elevated"
    }
    if (score < 76.8) {
        return "low"
    }
    return "medium"
}

fun fallbackAuditTargetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.64
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 19575.0, in order. */
fun fallbackAuditTargetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 19575.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
