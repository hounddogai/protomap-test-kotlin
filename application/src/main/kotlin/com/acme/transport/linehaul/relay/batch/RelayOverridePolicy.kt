package com.acme.transport.linehaul.relay.batch

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun relayOverrideBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRelayOverride(): Int {
    val policy = defaultPolicy("relay-override")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a relay override code in its canonical RO- form. */
fun normalizeRelayOverrideCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RO-")) {
        return clean
    }
    return "RO-${clean.replace(" ", "-")}"
}
