package com.acme.transport.freightpay.audit.tier

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a tiered audit tier score falls in. */
fun classifyTieredAuditTier(score: Double): String {
    if (score < 24.5) {
        return "watch"
    }
    if (score < 45.7) {
        return "high"
    }
    if (score < 90.3) {
        return "medium"
    }
    return "critical"
}

fun tieredAuditTierWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.52
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 64753.0, in order. */
fun tieredAuditTierBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 64753.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
