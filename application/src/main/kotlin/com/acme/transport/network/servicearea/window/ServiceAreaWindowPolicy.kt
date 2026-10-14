package com.acme.transport.network.servicearea.window

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeServiceAreaWindow(): Int {
    val policy = defaultPolicy("service-area-window")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun serviceAreaWindowBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Puts a service area window code in its canonical SAW- form. */
fun normalizeServiceAreaWindowCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SAW-")) {
        return clean
    }
    return "SAW-${clean.replace(" ", "-")}"
}
