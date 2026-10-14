package com.acme.transport.network.transittime.preference

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScheduledTransitTimeDigest(): Int {
    val policy = defaultPolicy("scheduled-transit-time-digest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun scheduledTransitTimeDigestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.06
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a scheduled transit time digest code in its canonical STTD- form. */
fun normalizeScheduledTransitTimeDigestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("STTD-")) {
        return clean
    }
    return "STTD-${clean.replace(" ", "-")}"
}
