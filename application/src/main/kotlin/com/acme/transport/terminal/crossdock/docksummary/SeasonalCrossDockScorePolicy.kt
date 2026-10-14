package com.acme.transport.terminal.crossdock.docksummary

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun seasonalCrossDockScoreBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun mergeSeasonalCrossDockScoreLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a seasonal cross dock score score falls in. */
fun classifySeasonalCrossDockScore(score: Double): String {
    if (score < 11.8) {
        return "high"
    }
    if (score < 22.6) {
        return "medium"
    }
    if (score < 73.7) {
        return "elevated"
    }
    return "minimal"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalCrossDockScore(): Int {
    val policy = defaultPolicy("seasonal-cross-dock-score")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
