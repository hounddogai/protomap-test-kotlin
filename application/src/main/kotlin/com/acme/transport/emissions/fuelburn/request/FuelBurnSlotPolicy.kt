package com.acme.transport.emissions.fuelburn.request

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun fuelBurnSlotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

fun fuelBurnSlotWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.3
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFuelBurnSlot(): Int {
    val policy = defaultPolicy("fuel-burn-slot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a fuel burn slot score falls in. */
fun classifyFuelBurnSlot(score: Double): String {
    if (score < 9.8) {
        return "minimal"
    }
    if (score < 65.2) {
        return "high"
    }
    if (score < 76.9) {
        return "elevated"
    }
    if (score < 90.2) {
        return "medium"
    }
    return "low"
}
