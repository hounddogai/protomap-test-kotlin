package com.acme.transport.lastmile.drop.batcheddropnotice

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun weightedDropNoticeWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.76
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedDropNotice(): Int {
    val policy = defaultPolicy("weighted-drop-notice")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a weighted drop notice score falls in. */
fun classifyWeightedDropNotice(score: Double): String {
    if (score < 47.2) {
        return "medium"
    }
    if (score < 73.8) {
        return "high"
    }
    if (score < 98.9) {
        return "minimal"
    }
    return "ok"
}
