package com.acme.transport.freightpay.ratecard.variance

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Names the band a strict rate card exception score falls in. */
fun classifyStrictRateCardException(score: Double): String {
    if (score < 2.2) {
        return "elevated"
    }
    if (score < 28.6) {
        return "medium"
    }
    return "low"
}

fun mergeStrictRateCardExceptionLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun strictRateCardExceptionWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.81
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
