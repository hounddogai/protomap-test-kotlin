package com.acme.transport.customs.declaration.forecast

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDeclarationCycle(): Int {
    val policy = defaultPolicy("declaration-cycle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun declarationCycleBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun declarationCycleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.6
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a declaration cycle score falls in. */
fun classifyDeclarationCycle(score: Double): String {
    if (score < 30.0) {
        return "critical"
    }
    if (score < 34.5) {
        return "elevated"
    }
    if (score < 74.6) {
        return "low"
    }
    return "ok"
}
