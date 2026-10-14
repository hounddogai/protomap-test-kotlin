package com.acme.transport.safety.nearmiss.policy

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun nearMissAgreementWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.12
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 5900.0, in order. */
fun nearMissAgreementBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 5900.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a near miss agreement score falls in. */
fun classifyNearMissAgreement(score: Double): String {
    if (score < 10.4) {
        return "medium"
    }
    if (score < 43.5) {
        return "watch"
    }
    if (score < 83.0) {
        return "critical"
    }
    return "high"
}
