package com.acme.transport.customs.broker.tier

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Puts a primary broker tier code in its canonical PBT- form. */
fun normalizePrimaryBrokerTierCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PBT-")) {
        return clean
    }
    return "PBT-${clean.replace(" ", "-")}"
}

fun primaryBrokerTierWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.18
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryBrokerTier(): Int {
    val policy = defaultPolicy("primary-broker-tier")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
