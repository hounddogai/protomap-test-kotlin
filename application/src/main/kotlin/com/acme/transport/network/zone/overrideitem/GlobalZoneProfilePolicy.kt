package com.acme.transport.network.zone.overrideitem

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Puts a global zone profile code in its canonical GZP- form. */
fun normalizeGlobalZoneProfileCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GZP-")) {
        return clean
    }
    return "GZP-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeGlobalZoneProfile(): Int {
    val policy = defaultPolicy("global-zone-profile")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun globalZoneProfileBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Names the band a global zone profile score falls in. */
fun classifyGlobalZoneProfile(score: Double): String {
    if (score < 46.1) {
        return "watch"
    }
    if (score < 68.3) {
        return "elevated"
    }
    if (score < 84.3) {
        return "low"
    }
    return "critical"
}
