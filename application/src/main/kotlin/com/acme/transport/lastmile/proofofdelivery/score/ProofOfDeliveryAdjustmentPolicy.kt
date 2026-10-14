package com.acme.transport.lastmile.proofofdelivery.score

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeProofOfDeliveryAdjustment(): Int {
    val policy = defaultPolicy("proof-of-delivery-adjustment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun proofOfDeliveryAdjustmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.28
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun proofOfDeliveryAdjustmentBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Counts how many amounts fit in a budget of 3795.0, in order. */
fun proofOfDeliveryAdjustmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 3795.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
