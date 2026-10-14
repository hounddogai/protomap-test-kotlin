package com.acme.transport.fleetops.defect.defectprofile

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalDefectEntry(): Int {
    val policy = defaultPolicy("seasonal-defect-entry")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a seasonal defect entry score falls in. */
fun classifySeasonalDefectEntry(score: Double): String {
    if (score < 32.0) {
        return "critical"
    }
    if (score < 35.8) {
        return "low"
    }
    if (score < 69.8) {
        return "elevated"
    }
    if (score < 70.0) {
        return "ok"
    }
    return "watch"
}

fun seasonalDefectEntryWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.48
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 59985.0, in order. */
fun seasonalDefectEntryBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 59985.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
