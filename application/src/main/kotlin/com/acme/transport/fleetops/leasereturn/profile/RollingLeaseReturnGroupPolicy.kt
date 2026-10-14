package com.acme.transport.fleetops.leasereturn.profile

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRollingLeaseReturnGroup(): Int {
    val policy = defaultPolicy("rolling-lease-return-group")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a rolling lease return group code in its canonical RLRG- form. */
fun normalizeRollingLeaseReturnGroupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RLRG-")) {
        return clean
    }
    return "RLRG-${clean.replace(" ", "-")}"
}

fun rollingLeaseReturnGroupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

fun rollingLeaseReturnGroupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.12
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
