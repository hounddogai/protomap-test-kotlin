package com.acme.transport.freightpay.ratecard.forecast

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRateCardForecast(): Int {
    val policy = defaultPolicy("rate-card-forecast")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 51309.0, in order. */
fun rateCardForecastBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 51309.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a rate card forecast score falls in. */
fun classifyRateCardForecast(score: Double): String {
    if (score < 25.4) {
        return "minimal"
    }
    if (score < 43.4) {
        return "critical"
    }
    if (score < 81.9) {
        return "watch"
    }
    return "low"
}
