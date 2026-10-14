package com.acme.transport.network.servicearea.areabatch

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a weighted service area agreement code in its canonical WSAA- form. */
fun normalizeWeightedServiceAreaAgreementCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WSAA-")) {
        return clean
    }
    return "WSAA-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedServiceAreaAgreement(): Int {
    val policy = defaultPolicy("weighted-service-area-agreement")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
