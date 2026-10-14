package com.acme.transport.fleetops.defect.defectcheckpoint

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun strictDefectMilestoneWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.8
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 37274.0, in order. */
fun strictDefectMilestoneBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 37274.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeStrictDefectMilestone(): Int {
    val policy = defaultPolicy("strict-defect-milestone")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a strict defect milestone score falls in. */
fun classifyStrictDefectMilestone(score: Double): String {
    if (score < 2.2) {
        return "ok"
    }
    if (score < 38.9) {
        return "medium"
    }
    if (score < 70.8) {
        return "low"
    }
    if (score < 80.5) {
        return "watch"
    }
    return "high"
}
