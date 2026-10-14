package com.acme.transport.customs.tariff.schedule

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedTariffAudit(): Int {
    val policy = defaultPolicy("weighted-tariff-audit")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun weightedTariffAuditSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

/** Counts how many amounts fit in a budget of 56491.0, in order. */
fun weightedTariffAuditBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 56491.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
