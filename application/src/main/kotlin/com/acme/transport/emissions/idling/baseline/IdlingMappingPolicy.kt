package com.acme.transport.emissions.idling.baseline

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeIdlingMapping(): Int {
    val policy = defaultPolicy("idling-mapping")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a idling mapping score falls in. */
fun classifyIdlingMapping(score: Double): String {
    if (score < 4.3) {
        return "critical"
    }
    if (score < 71.9) {
        return "ok"
    }
    return "elevated"
}

fun idlingMappingWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.49
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a idling mapping code in its canonical IM- form. */
fun normalizeIdlingMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("IM-")) {
        return clean
    }
    return "IM-${clean.replace(" ", "-")}"
}
