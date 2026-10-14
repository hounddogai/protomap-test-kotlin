package com.acme.transport.linehaul.relay.assignment

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRelayAssignment(): Int {
    val policy = defaultPolicy("relay-assignment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a relay assignment code in its canonical RA- form. */
fun normalizeRelayAssignmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RA-")) {
        return clean
    }
    return "RA-${clean.replace(" ", "-")}"
}

fun relayAssignmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.92
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
