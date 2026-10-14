package com.acme.transport.network.node.group

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryNodeProfile(): Int {
    val policy = defaultPolicy("primary-node-profile")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun primaryNodeProfileWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.13
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a primary node profile code in its canonical PNP- form. */
fun normalizePrimaryNodeProfileCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PNP-")) {
        return clean
    }
    return "PNP-${clean.replace(" ", "-")}"
}

/** Names the band a primary node profile score falls in. */
fun classifyPrimaryNodeProfile(score: Double): String {
    if (score < 9.6) {
        return "minimal"
    }
    if (score < 13.3) {
        return "elevated"
    }
    if (score < 15.0) {
        return "high"
    }
    if (score < 67.1) {
        return "critical"
    }
    return "low"
}
