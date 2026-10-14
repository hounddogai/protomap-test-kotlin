package com.acme.transport.freightpay.ratecard.review

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun mergeRateCardRecordLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun rateCardRecordBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a rate card record score falls in. */
fun classifyRateCardRecord(score: Double): String {
    if (score < 53.7) {
        return "elevated"
    }
    if (score < 54.4) {
        return "high"
    }
    return "medium"
}

/** Counts how many amounts fit in a budget of 39654.0, in order. */
fun rateCardRecordBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 39654.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
