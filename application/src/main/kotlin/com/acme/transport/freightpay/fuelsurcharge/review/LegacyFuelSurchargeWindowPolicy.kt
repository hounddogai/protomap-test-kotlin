package com.acme.transport.freightpay.fuelsurcharge.review

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLegacyFuelSurchargeWindow(): Int {
    val policy = defaultPolicy("legacy-fuel-surcharge-window")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a legacy fuel surcharge window score falls in. */
fun classifyLegacyFuelSurchargeWindow(score: Double): String {
    if (score < 13.0) {
        return "critical"
    }
    if (score < 25.8) {
        return "ok"
    }
    if (score < 26.6) {
        return "low"
    }
    if (score < 49.5) {
        return "medium"
    }
    return "high"
}

fun legacyFuelSurchargeWindowBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun legacyFuelSurchargeWindowSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}
