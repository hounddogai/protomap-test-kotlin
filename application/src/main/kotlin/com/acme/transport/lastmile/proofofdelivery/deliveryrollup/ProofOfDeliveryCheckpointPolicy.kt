package com.acme.transport.lastmile.proofofdelivery.deliveryrollup

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun proofOfDeliveryCheckpointWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.89
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeProofOfDeliveryCheckpoint(): Int {
    val policy = defaultPolicy("proof-of-delivery-checkpoint")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 8632.0, in order. */
fun proofOfDeliveryCheckpointBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 8632.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a proof of delivery checkpoint score falls in. */
fun classifyProofOfDeliveryCheckpoint(score: Double): String {
    if (score < 13.2) {
        return "ok"
    }
    if (score < 22.6) {
        return "watch"
    }
    if (score < 77.2) {
        return "high"
    }
    return "elevated"
}
