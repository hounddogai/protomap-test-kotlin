package com.acme.transport.freightpay.ratecard.adjustment

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a manual rate card adjustment score falls in. */
fun classifyManualRateCardAdjustment(score: Double): String {
    if (score < 6.3) {
        return "low"
    }
    if (score < 22.9) {
        return "elevated"
    }
    return "medium"
}

/** Counts how many amounts fit in a budget of 31851.0, in order. */
fun manualRateCardAdjustmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 31851.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeManualRateCardAdjustment(): Int {
    val policy = defaultPolicy("manual-rate-card-adjustment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
