package com.acme.transport.emissions.idling.idlingquota

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun manualIdlingQuotaBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeManualIdlingQuota(): Int {
    val policy = defaultPolicy("manual-idling-quota")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a manual idling quota code in its canonical MIQ- form. */
fun normalizeManualIdlingQuotaCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MIQ-")) {
        return clean
    }
    return "MIQ-${clean.replace(" ", "-")}"
}
