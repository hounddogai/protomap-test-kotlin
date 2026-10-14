package com.acme.transport.linehaul.backhaul.ledger

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun scheduledBackhaulAuditBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScheduledBackhaulAudit(): Int {
    val policy = defaultPolicy("scheduled-backhaul-audit")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
