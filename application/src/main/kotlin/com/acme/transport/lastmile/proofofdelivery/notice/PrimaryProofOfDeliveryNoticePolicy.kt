package com.acme.transport.lastmile.proofofdelivery.notice

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryProofOfDeliveryNotice(): Int {
    val policy = defaultPolicy("primary-proof-of-delivery-notice")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun primaryProofOfDeliveryNoticeWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.74
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 70514.0, in order. */
fun primaryProofOfDeliveryNoticeBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 70514.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
