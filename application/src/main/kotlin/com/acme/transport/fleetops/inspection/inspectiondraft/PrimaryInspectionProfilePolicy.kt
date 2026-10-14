package com.acme.transport.fleetops.inspection.inspectiondraft

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Names the band a primary inspection profile score falls in. */
fun classifyPrimaryInspectionProfile(score: Double): String {
    if (score < 29.1) {
        return "ok"
    }
    if (score < 39.2) {
        return "minimal"
    }
    if (score < 91.5) {
        return "elevated"
    }
    return "high"
}

fun mergePrimaryInspectionProfileLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun primaryInspectionProfileWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.92
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 71851.0, in order. */
fun primaryInspectionProfileBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 71851.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
