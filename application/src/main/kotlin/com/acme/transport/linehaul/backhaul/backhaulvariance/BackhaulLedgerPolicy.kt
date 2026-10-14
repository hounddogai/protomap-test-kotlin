package com.acme.transport.linehaul.backhaul.backhaulvariance

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun backhaulLedgerBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Names the band a backhaul ledger score falls in. */
fun classifyBackhaulLedger(score: Double): String {
    if (score < 17.9) {
        return "elevated"
    }
    if (score < 33.0) {
        return "watch"
    }
    if (score < 66.3) {
        return "minimal"
    }
    if (score < 94.8) {
        return "high"
    }
    return "medium"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBackhaulLedger(): Int {
    val policy = defaultPolicy("backhaul-ledger")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
