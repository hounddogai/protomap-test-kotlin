package com.acme.transport.emissions.idling.regionalidlingmanifest

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRegionalIdlingException(): Int {
    val policy = defaultPolicy("regional-idling-exception")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun regionalIdlingExceptionBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Puts a regional idling exception code in its canonical RIE- form. */
fun normalizeRegionalIdlingExceptionCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RIE-")) {
        return clean
    }
    return "RIE-${clean.replace(" ", "-")}"
}
