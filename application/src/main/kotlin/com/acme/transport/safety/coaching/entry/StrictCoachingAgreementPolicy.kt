package com.acme.transport.safety.coaching.entry

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Names the band a strict coaching agreement score falls in. */
fun classifyStrictCoachingAgreement(score: Double): String {
    if (score < 23.3) {
        return "medium"
    }
    if (score < 39.7) {
        return "minimal"
    }
    return "low"
}

fun mergeStrictCoachingAgreementLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 44163.0, in order. */
fun strictCoachingAgreementBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 44163.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun strictCoachingAgreementWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.34
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
