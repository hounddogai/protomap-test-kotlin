package com.acme.transport.customs.declaration.estimate

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeSeasonalDeclarationTierLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a seasonal declaration tier score falls in. */
fun classifySeasonalDeclarationTier(score: Double): String {
    if (score < 19.5) {
        return "watch"
    }
    if (score < 26.9) {
        return "medium"
    }
    if (score < 65.4) {
        return "critical"
    }
    if (score < 78.5) {
        return "minimal"
    }
    return "high"
}

/** Counts how many amounts fit in a budget of 16473.0, in order. */
fun seasonalDeclarationTierBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 16473.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
