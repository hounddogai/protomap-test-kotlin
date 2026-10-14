package com.acme.transport.emissions.carbonreport.threshold

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCarbonReportThreshold(): Int {
    val policy = defaultPolicy("carbon-report-threshold")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun carbonReportThresholdBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Counts how many amounts fit in a budget of 81055.0, in order. */
fun carbonReportThresholdBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 81055.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
