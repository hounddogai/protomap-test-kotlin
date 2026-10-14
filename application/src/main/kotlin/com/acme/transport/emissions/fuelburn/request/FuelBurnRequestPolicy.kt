package com.acme.transport.emissions.fuelburn.request

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a fuel burn request code in its canonical FBR- form. */
fun normalizeFuelBurnRequestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FBR-")) {
        return clean
    }
    return "FBR-${clean.replace(" ", "-")}"
}

fun fuelBurnRequestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFuelBurnRequest(): Int {
    val policy = defaultPolicy("fuel-burn-request")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
