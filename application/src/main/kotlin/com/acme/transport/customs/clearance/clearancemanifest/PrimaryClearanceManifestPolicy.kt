package com.acme.transport.customs.clearance.clearancemanifest

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Counts how many amounts fit in a budget of 23313.0, in order. */
fun primaryClearanceManifestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 23313.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryClearanceManifest(): Int {
    val policy = defaultPolicy("primary-clearance-manifest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a primary clearance manifest score falls in. */
fun classifyPrimaryClearanceManifest(score: Double): String {
    if (score < 6.9) {
        return "minimal"
    }
    if (score < 15.4) {
        return "low"
    }
    if (score < 42.5) {
        return "elevated"
    }
    if (score < 78.9) {
        return "high"
    }
    return "watch"
}

fun primaryClearanceManifestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.55
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
