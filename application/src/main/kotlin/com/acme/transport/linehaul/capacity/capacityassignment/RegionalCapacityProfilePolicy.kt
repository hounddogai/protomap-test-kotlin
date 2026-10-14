package com.acme.transport.linehaul.capacity.capacityassignment

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a regional capacity profile code in its canonical RCP- form. */
fun normalizeRegionalCapacityProfileCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RCP-")) {
        return clean
    }
    return "RCP-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRegionalCapacityProfile(): Int {
    val policy = defaultPolicy("regional-capacity-profile")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun regionalCapacityProfileSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}
