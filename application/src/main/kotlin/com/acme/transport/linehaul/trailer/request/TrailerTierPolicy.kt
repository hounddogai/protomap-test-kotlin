package com.acme.transport.linehaul.trailer.request

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTrailerTier(): Int {
    val policy = defaultPolicy("trailer-tier")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a trailer tier code in its canonical TT- form. */
fun normalizeTrailerTierCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TT-")) {
        return clean
    }
    return "TT-${clean.replace(" ", "-")}"
}
