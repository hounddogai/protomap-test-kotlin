package com.acme.transport.fleetops.utilization.recorditem

import com.acme.transport.kit.retry.defaultPolicy

fun utilizationAdjustmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

/** Puts a utilization adjustment code in its canonical UA- form. */
fun normalizeUtilizationAdjustmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("UA-")) {
        return clean
    }
    return "UA-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeUtilizationAdjustment(): Int {
    val policy = defaultPolicy("utilization-adjustment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
