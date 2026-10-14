package com.acme.transport.emissions.emissionfactor.template

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun weightedEmissionFactorTemplateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.89
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 64824.0, in order. */
fun weightedEmissionFactorTemplateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 64824.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a weighted emission factor template score falls in. */
fun classifyWeightedEmissionFactorTemplate(score: Double): String {
    if (score < 3.1) {
        return "high"
    }
    if (score < 18.7) {
        return "critical"
    }
    if (score < 96.7) {
        return "low"
    }
    return "watch"
}

fun mergeWeightedEmissionFactorTemplateLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
