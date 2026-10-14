package com.acme.transport.freightpay.accessorial.accessorialcycle

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRegionalAccessorialCycle(): Int {
    val policy = defaultPolicy("regional-accessorial-cycle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a regional accessorial cycle code in its canonical RAC- form. */
fun normalizeRegionalAccessorialCycleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RAC-")) {
        return clean
    }
    return "RAC-${clean.replace(" ", "-")}"
}

fun regionalAccessorialCycleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}
