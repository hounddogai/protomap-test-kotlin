package com.acme.transport.linehaul.relay.relayquota

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRollingRelayRequest(): Int {
    val policy = defaultPolicy("rolling-relay-request")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a rolling relay request score falls in. */
fun classifyRollingRelayRequest(score: Double): String {
    if (score < 19.8) {
        return "ok"
    }
    if (score < 64.0) {
        return "high"
    }
    return "low"
}

fun mergeRollingRelayRequestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
