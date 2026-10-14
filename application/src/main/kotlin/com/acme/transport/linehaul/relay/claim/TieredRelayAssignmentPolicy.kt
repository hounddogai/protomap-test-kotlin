package com.acme.transport.linehaul.relay.claim

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Puts a tiered relay assignment code in its canonical TRA- form. */
fun normalizeTieredRelayAssignmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TRA-")) {
        return clean
    }
    return "TRA-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTieredRelayAssignment(): Int {
    val policy = defaultPolicy("tiered-relay-assignment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun tieredRelayAssignmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.03
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
