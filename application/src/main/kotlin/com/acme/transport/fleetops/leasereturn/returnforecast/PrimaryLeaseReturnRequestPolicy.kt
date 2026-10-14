package com.acme.transport.fleetops.leasereturn.returnforecast

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a primary lease return request score falls in. */
fun classifyPrimaryLeaseReturnRequest(score: Double): String {
    if (score < 12.8) {
        return "minimal"
    }
    if (score < 68.5) {
        return "watch"
    }
    if (score < 85.4) {
        return "high"
    }
    return "elevated"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryLeaseReturnRequest(): Int {
    val policy = defaultPolicy("primary-lease-return-request")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a primary lease return request code in its canonical PLRR- form. */
fun normalizePrimaryLeaseReturnRequestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PLRR-")) {
        return clean
    }
    return "PLRR-${clean.replace(" ", "-")}"
}
