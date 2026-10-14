package com.acme.transport.linehaul.tender.assignment

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun primaryTenderQuotaBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryTenderQuota(): Int {
    val policy = defaultPolicy("primary-tender-quota")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a primary tender quota score falls in. */
fun classifyPrimaryTenderQuota(score: Double): String {
    if (score < 31.0) {
        return "watch"
    }
    if (score < 59.7) {
        return "ok"
    }
    return "medium"
}
