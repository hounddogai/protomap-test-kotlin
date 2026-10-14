package com.acme.transport.terminal.dwell.score

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probeManualDwellProfile(): Int {
    val policy = defaultPolicy("manual-dwell-profile")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a manual dwell profile score falls in. */
fun classifyManualDwellProfile(score: Double): String {
    if (score < 3.9) {
        return "critical"
    }
    if (score < 12.8) {
        return "ok"
    }
    return "elevated"
}

fun manualDwellProfileBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun mergeManualDwellProfileLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
