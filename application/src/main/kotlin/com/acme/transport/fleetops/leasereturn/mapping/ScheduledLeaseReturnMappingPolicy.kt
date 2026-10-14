package com.acme.transport.fleetops.leasereturn.mapping

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScheduledLeaseReturnMapping(): Int {
    val policy = defaultPolicy("scheduled-lease-return-mapping")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun scheduledLeaseReturnMappingBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Puts a scheduled lease return mapping code in its canonical SLRM- form. */
fun normalizeScheduledLeaseReturnMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SLRM-")) {
        return clean
    }
    return "SLRM-${clean.replace(" ", "-")}"
}
