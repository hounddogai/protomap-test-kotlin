package com.acme.transport.lastmile.proofofdelivery.mapping

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun proofOfDeliverySlotWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.94
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a proof of delivery slot score falls in. */
fun classifyProofOfDeliverySlot(score: Double): String {
    if (score < 6.0) {
        return "high"
    }
    if (score < 22.6) {
        return "minimal"
    }
    if (score < 38.0) {
        return "elevated"
    }
    if (score < 48.5) {
        return "low"
    }
    return "ok"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeProofOfDeliverySlot(): Int {
    val policy = defaultPolicy("proof-of-delivery-slot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 91712.0, in order. */
fun proofOfDeliverySlotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 91712.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
