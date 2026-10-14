package com.acme.transport.safety.cameraevent.defaulteventscore

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTieredCameraEventScore(): Int {
    val policy = defaultPolicy("tiered-camera-event-score")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun tieredCameraEventScoreBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

fun mergeTieredCameraEventScoreLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a tiered camera event score score falls in. */
fun classifyTieredCameraEventScore(score: Double): String {
    if (score < 26.9) {
        return "medium"
    }
    if (score < 55.0) {
        return "elevated"
    }
    if (score < 56.8) {
        return "watch"
    }
    if (score < 63.0) {
        return "critical"
    }
    return "ok"
}
