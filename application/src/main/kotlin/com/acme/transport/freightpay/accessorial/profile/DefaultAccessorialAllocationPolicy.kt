package com.acme.transport.freightpay.accessorial.profile

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun defaultAccessorialAllocationBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Puts a default accessorial allocation code in its canonical DAA- form. */
fun normalizeDefaultAccessorialAllocationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DAA-")) {
        return clean
    }
    return "DAA-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDefaultAccessorialAllocation(): Int {
    val policy = defaultPolicy("default-accessorial-allocation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
