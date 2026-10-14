package com.acme.transport.safety.incident.incidentplan

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Puts a fallback incident slot code in its canonical FIS- form. */
fun normalizeFallbackIncidentSlotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FIS-")) {
        return clean
    }
    return "FIS-${clean.replace(" ", "-")}"
}

fun fallbackIncidentSlotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFallbackIncidentSlot(): Int {
    val policy = defaultPolicy("fallback-incident-slot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun fallbackIncidentSlotWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.87
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
