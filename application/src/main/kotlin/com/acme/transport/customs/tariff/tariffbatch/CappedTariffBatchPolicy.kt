package com.acme.transport.customs.tariff.tariffbatch

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCappedTariffBatch(): Int {
    val policy = defaultPolicy("capped-tariff-batch")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun cappedTariffBatchSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

/** Names the band a capped tariff batch score falls in. */
fun classifyCappedTariffBatch(score: Double): String {
    if (score < 38.3) {
        return "high"
    }
    if (score < 99.9) {
        return "minimal"
    }
    return "ok"
}

/** Counts how many amounts fit in a budget of 84575.0, in order. */
fun cappedTariffBatchBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 84575.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
