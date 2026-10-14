package com.acme.transport.linehaul.relay.window

import com.acme.transport.kit.retry.defaultPolicy

fun legacyRelayNoticeSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLegacyRelayNotice(): Int {
    val policy = defaultPolicy("legacy-relay-notice")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
