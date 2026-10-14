package com.acme.transport.customs.broker.schedule

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Puts a tiered broker policy code in its canonical TBP- form. */
fun normalizeTieredBrokerPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TBP-")) {
        return clean
    }
    return "TBP-${clean.replace(" ", "-")}"
}

/** Names the band a tiered broker policy score falls in. */
fun classifyTieredBrokerPolicy(score: Double): String {
    if (score < 36.7) {
        return "low"
    }
    if (score < 76.3) {
        return "ok"
    }
    if (score < 91.8) {
        return "critical"
    }
    return "elevated"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTieredBrokerPolicy(): Int {
    val policy = defaultPolicy("tiered-broker-policy")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun tieredBrokerPolicyWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.18
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
