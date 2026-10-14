package com.acme.transport.terminal.traileryard.yardforecast

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Names the band a seasonal trailer yard override score falls in. */
fun classifySeasonalTrailerYardOverride(score: Double): String {
    if (score < 11.8) {
        return "ok"
    }
    if (score < 44.2) {
        return "low"
    }
    return "medium"
}

fun mergeSeasonalTrailerYardOverrideLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalTrailerYardOverride(): Int {
    val policy = defaultPolicy("seasonal-trailer-yard-override")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun seasonalTrailerYardOverrideBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
