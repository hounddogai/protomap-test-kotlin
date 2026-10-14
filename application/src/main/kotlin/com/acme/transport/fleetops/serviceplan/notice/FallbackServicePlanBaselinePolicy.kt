package com.acme.transport.fleetops.serviceplan.notice

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun fallbackServicePlanBaselineBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Puts a fallback service plan baseline code in its canonical FSPB- form. */
fun normalizeFallbackServicePlanBaselineCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FSPB-")) {
        return clean
    }
    return "FSPB-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFallbackServicePlanBaseline(): Int {
    val policy = defaultPolicy("fallback-service-plan-baseline")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
