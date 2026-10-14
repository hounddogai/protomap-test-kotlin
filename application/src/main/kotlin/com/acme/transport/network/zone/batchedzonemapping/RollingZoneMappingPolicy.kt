package com.acme.transport.network.zone.batchedzonemapping

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a rolling zone mapping score falls in. */
fun classifyRollingZoneMapping(score: Double): String {
    if (score < 50.6) {
        return "high"
    }
    if (score < 79.3) {
        return "watch"
    }
    if (score < 95.2) {
        return "elevated"
    }
    return "minimal"
}

/** Puts a rolling zone mapping code in its canonical RZM- form. */
fun normalizeRollingZoneMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RZM-")) {
        return clean
    }
    return "RZM-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRollingZoneMapping(): Int {
    val policy = defaultPolicy("rolling-zone-mapping")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
