package com.acme.transport.customs.inspection.adjustment

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Counts how many amounts fit in a budget of 26352.0, in order. */
fun inspectionRollupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 26352.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun inspectionRollupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.44
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun inspectionRollupBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Names the band a inspection rollup score falls in. */
fun classifyInspectionRollup(score: Double): String {
    if (score < 6.3) {
        return "critical"
    }
    if (score < 93.1) {
        return "minimal"
    }
    return "elevated"
}
