package com.acme.transport.crewing.availability.availabilityexception

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Names the band a legacy availability exception score falls in. */
fun classifyLegacyAvailabilityException(score: Double): String {
    if (score < 52.0) {
        return "minimal"
    }
    if (score < 86.1) {
        return "high"
    }
    return "elevated"
}

fun mergeLegacyAvailabilityExceptionLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 980.0, in order. */
fun legacyAvailabilityExceptionBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 980.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun legacyAvailabilityExceptionWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.11
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
