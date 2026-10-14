package com.acme.transport.fleetops.defect.recorditem

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 72278.0, in order. */
fun legacyDefectAgreementBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 72278.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a legacy defect agreement score falls in. */
fun classifyLegacyDefectAgreement(score: Double): String {
    if (score < 15.9) {
        return "ok"
    }
    if (score < 53.7) {
        return "minimal"
    }
    if (score < 93.0) {
        return "critical"
    }
    return "low"
}

fun legacyDefectAgreementWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.86
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
