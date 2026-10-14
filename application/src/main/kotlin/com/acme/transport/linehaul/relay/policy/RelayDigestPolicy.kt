package com.acme.transport.linehaul.relay.policy

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a relay digest code in its canonical RD- form. */
fun normalizeRelayDigestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RD-")) {
        return clean
    }
    return "RD-${clean.replace(" ", "-")}"
}

fun relayDigestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRelayDigest(): Int {
    val policy = defaultPolicy("relay-digest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
