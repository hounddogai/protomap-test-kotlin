package com.acme.transport.terminal.crossdock.schedule

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Puts a regional cross dock quota code in its canonical RCDQ- form. */
fun normalizeRegionalCrossDockQuotaCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RCDQ-")) {
        return clean
    }
    return "RCDQ-${clean.replace(" ", "-")}"
}

fun regionalCrossDockQuotaBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRegionalCrossDockQuota(): Int {
    val policy = defaultPolicy("regional-cross-dock-quota")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
