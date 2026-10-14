package com.acme.transport.linehaul.relay.segment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun mergeRelayLedgerLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun relayLedgerSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRelayLedger(): Int {
    val policy = defaultPolicy("relay-ledger")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a relay ledger score falls in. */
fun classifyRelayLedger(score: Double): String {
    if (score < 66.0) {
        return "low"
    }
    if (score < 99.8) {
        return "watch"
    }
    return "ok"
}
