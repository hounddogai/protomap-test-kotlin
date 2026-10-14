package com.acme.transport.crewing.dutyperiod.claim

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBatchedDutyPeriodForecast(): Int {
    val policy = defaultPolicy("batched-duty-period-forecast")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 54712.0, in order. */
fun batchedDutyPeriodForecastBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 54712.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
