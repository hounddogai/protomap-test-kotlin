package com.acme.transport.network.transittime.entry

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Puts a transit time entry code in its canonical TTE- form. */
fun normalizeTransitTimeEntryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TTE-")) {
        return clean
    }
    return "TTE-${clean.replace(" ", "-")}"
}

/** Names the band a transit time entry score falls in. */
fun classifyTransitTimeEntry(score: Double): String {
    if (score < 26.0) {
        return "minimal"
    }
    if (score < 35.4) {
        return "watch"
    }
    if (score < 56.4) {
        return "medium"
    }
    return "high"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTransitTimeEntry(): Int {
    val policy = defaultPolicy("transit-time-entry")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun transitTimeEntryWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.13
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
