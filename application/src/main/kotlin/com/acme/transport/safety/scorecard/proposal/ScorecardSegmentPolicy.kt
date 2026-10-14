package com.acme.transport.safety.scorecard.proposal

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Names the band a scorecard segment score falls in. */
fun classifyScorecardSegment(score: Double): String {
    if (score < 4.7) {
        return "ok"
    }
    if (score < 35.8) {
        return "medium"
    }
    if (score < 69.7) {
        return "high"
    }
    return "low"
}

fun mergeScorecardSegmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun scorecardSegmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.29
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 97744.0, in order. */
fun scorecardSegmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 97744.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
