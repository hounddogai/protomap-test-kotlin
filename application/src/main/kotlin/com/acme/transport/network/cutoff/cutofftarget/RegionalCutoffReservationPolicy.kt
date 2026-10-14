package com.acme.transport.network.cutoff.cutofftarget

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Names the band a regional cutoff reservation score falls in. */
fun classifyRegionalCutoffReservation(score: Double): String {
    if (score < 21.0) {
        return "elevated"
    }
    if (score < 36.3) {
        return "ok"
    }
    if (score < 57.9) {
        return "watch"
    }
    if (score < 96.7) {
        return "critical"
    }
    return "high"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRegionalCutoffReservation(): Int {
    val policy = defaultPolicy("regional-cutoff-reservation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeRegionalCutoffReservationLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
