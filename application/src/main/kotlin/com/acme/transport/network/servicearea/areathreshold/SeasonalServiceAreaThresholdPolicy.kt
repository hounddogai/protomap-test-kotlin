package com.acme.transport.network.servicearea.areathreshold

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a seasonal service area threshold code in its canonical SSAT- form. */
fun normalizeSeasonalServiceAreaThresholdCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SSAT-")) {
        return clean
    }
    return "SSAT-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalServiceAreaThreshold(): Int {
    val policy = defaultPolicy("seasonal-service-area-threshold")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
