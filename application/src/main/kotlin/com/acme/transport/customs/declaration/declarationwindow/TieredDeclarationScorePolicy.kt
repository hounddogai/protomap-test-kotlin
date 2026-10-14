package com.acme.transport.customs.declaration.declarationwindow

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeTieredDeclarationScoreLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 10136.0, in order. */
fun tieredDeclarationScoreBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 10136.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun tieredDeclarationScoreSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

/** Names the band a tiered declaration score score falls in. */
fun classifyTieredDeclarationScore(score: Double): String {
    if (score < 18.3) {
        return "ok"
    }
    if (score < 42.2) {
        return "minimal"
    }
    if (score < 84.8) {
        return "elevated"
    }
    return "medium"
}
