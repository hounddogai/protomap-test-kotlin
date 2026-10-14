package com.acme.transport.terminal.traileryard.adjustment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeStrictTrailerYardAuditLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a strict trailer yard audit score falls in. */
fun classifyStrictTrailerYardAudit(score: Double): String {
    if (score < 28.6) {
        return "minimal"
    }
    if (score < 51.8) {
        return "low"
    }
    return "medium"
}

fun strictTrailerYardAuditWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.3
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 60655.0, in order. */
fun strictTrailerYardAuditBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 60655.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
