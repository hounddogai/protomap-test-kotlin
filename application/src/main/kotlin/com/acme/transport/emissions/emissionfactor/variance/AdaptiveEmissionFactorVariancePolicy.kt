package com.acme.transport.emissions.emissionfactor.variance

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeAdaptiveEmissionFactorVarianceLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun adaptiveEmissionFactorVarianceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.53
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 3920.0, in order. */
fun adaptiveEmissionFactorVarianceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 3920.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a adaptive emission factor variance code in its canonical AEFV- form. */
fun normalizeAdaptiveEmissionFactorVarianceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AEFV-")) {
        return clean
    }
    return "AEFV-${clean.replace(" ", "-")}"
}
