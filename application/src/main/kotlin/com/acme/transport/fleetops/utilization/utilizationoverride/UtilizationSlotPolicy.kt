package com.acme.transport.fleetops.utilization.utilizationoverride

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a utilization slot code in its canonical US- form. */
fun normalizeUtilizationSlotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("US-")) {
        return clean
    }
    return "US-${clean.replace(" ", "-")}"
}

fun utilizationSlotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeUtilizationSlot(): Int {
    val policy = defaultPolicy("utilization-slot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
