package com.acme.transport.network.servicearea.manualareathreshold

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun primaryServiceAreaThresholdBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryServiceAreaThreshold(): Int {
    val policy = defaultPolicy("primary-service-area-threshold")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a primary service area threshold code in its canonical PSAT- form. */
fun normalizePrimaryServiceAreaThresholdCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PSAT-")) {
        return clean
    }
    return "PSAT-${clean.replace(" ", "-")}"
}
