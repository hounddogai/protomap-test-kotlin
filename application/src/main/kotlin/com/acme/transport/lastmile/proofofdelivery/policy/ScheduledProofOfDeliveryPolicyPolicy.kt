package com.acme.transport.lastmile.proofofdelivery.policy

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 53477.0, in order. */
fun scheduledProofOfDeliveryPolicyBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 53477.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScheduledProofOfDeliveryPolicy(): Int {
    val policy = defaultPolicy("scheduled-proof-of-delivery-policy")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a scheduled proof of delivery policy score falls in. */
fun classifyScheduledProofOfDeliveryPolicy(score: Double): String {
    if (score < 46.2) {
        return "high"
    }
    if (score < 50.4) {
        return "critical"
    }
    if (score < 76.7) {
        return "medium"
    }
    return "watch"
}
