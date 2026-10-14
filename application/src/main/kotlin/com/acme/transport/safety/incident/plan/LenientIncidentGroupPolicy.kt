package com.acme.transport.safety.incident.plan

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientIncidentGroup(): Int {
    val policy = defaultPolicy("lenient-incident-group")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun lenientIncidentGroupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.05
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a lenient incident group score falls in. */
fun classifyLenientIncidentGroup(score: Double): String {
    if (score < 32.5) {
        return "low"
    }
    if (score < 40.8) {
        return "watch"
    }
    if (score < 62.1) {
        return "ok"
    }
    if (score < 84.0) {
        return "elevated"
    }
    return "medium"
}

/** Puts a lenient incident group code in its canonical LIG- form. */
fun normalizeLenientIncidentGroupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LIG-")) {
        return clean
    }
    return "LIG-${clean.replace(" ", "-")}"
}
