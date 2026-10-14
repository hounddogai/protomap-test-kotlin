package com.acme.transport.fleetops.tire.profile

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Names the band a weighted tire score score falls in. */
fun classifyWeightedTireScore(score: Double): String {
    if (score < 42.2) {
        return "medium"
    }
    if (score < 66.9) {
        return "watch"
    }
    return "ok"
}

/** Counts how many amounts fit in a budget of 8103.0, in order. */
fun weightedTireScoreBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 8103.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedTireScore(): Int {
    val policy = defaultPolicy("weighted-tire-score")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun weightedTireScoreWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.75
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
