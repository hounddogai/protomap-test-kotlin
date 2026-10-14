package com.acme.transport.safety.incident.overrideitem

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalIncidentOverride(): Int {
    val policy = defaultPolicy("seasonal-incident-override")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun seasonalIncidentOverrideBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Puts a seasonal incident override code in its canonical SIO- form. */
fun normalizeSeasonalIncidentOverrideCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SIO-")) {
        return clean
    }
    return "SIO-${clean.replace(" ", "-")}"
}
