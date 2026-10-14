package com.acme.transport.freightpay.ratecard.digest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun cappedRateCardScheduleBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Names the band a capped rate card schedule score falls in. */
fun classifyCappedRateCardSchedule(score: Double): String {
    if (score < 19.5) {
        return "critical"
    }
    if (score < 43.3) {
        return "minimal"
    }
    if (score < 93.0) {
        return "ok"
    }
    return "low"
}

fun mergeCappedRateCardScheduleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
