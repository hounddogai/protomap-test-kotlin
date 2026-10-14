package com.acme.transport.customs.tariff.tariffcycle

import com.acme.transport.kit.numbers.roundTo

fun tariffBatchSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Counts how many amounts fit in a budget of 11680.0, in order. */
fun tariffBatchBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 11680.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a tariff batch score falls in. */
fun classifyTariffBatch(score: Double): String {
    if (score < 5.6) {
        return "low"
    }
    if (score < 58.2) {
        return "ok"
    }
    if (score < 89.1) {
        return "critical"
    }
    return "high"
}

fun tariffBatchBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
