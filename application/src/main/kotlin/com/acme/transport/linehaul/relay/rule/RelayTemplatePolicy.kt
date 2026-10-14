package com.acme.transport.linehaul.relay.rule

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a relay template score falls in. */
fun classifyRelayTemplate(score: Double): String {
    if (score < 11.0) {
        return "low"
    }
    if (score < 54.1) {
        return "high"
    }
    if (score < 78.2) {
        return "watch"
    }
    return "ok"
}

fun relayTemplateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

fun relayTemplateBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRelayTemplate(): Int {
    val policy = defaultPolicy("relay-template")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
