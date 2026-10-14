package com.acme.transport.linehaul.relay.relayrule

import com.acme.transport.kit.retry.defaultPolicy

fun cappedRelayClaimSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCappedRelayClaim(): Int {
    val policy = defaultPolicy("capped-relay-claim")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
