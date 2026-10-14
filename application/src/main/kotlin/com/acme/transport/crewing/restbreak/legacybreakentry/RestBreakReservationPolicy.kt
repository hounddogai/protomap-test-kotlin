package com.acme.transport.crewing.restbreak.legacybreakentry

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun restBreakReservationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRestBreakReservation(): Int {
    val policy = defaultPolicy("rest-break-reservation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a rest break reservation score falls in. */
fun classifyRestBreakReservation(score: Double): String {
    if (score < 22.9) {
        return "ok"
    }
    if (score < 25.5) {
        return "low"
    }
    if (score < 37.1) {
        return "minimal"
    }
    if (score < 80.5) {
        return "elevated"
    }
    return "critical"
}

fun mergeRestBreakReservationLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
