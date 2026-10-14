package com.acme.transport.customs.declaration.rule

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun declarationRuleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.05
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a declaration rule score falls in. */
fun classifyDeclarationRule(score: Double): String {
    if (score < 10.7) {
        return "watch"
    }
    if (score < 23.7) {
        return "minimal"
    }
    return "medium"
}

/** Counts how many amounts fit in a budget of 33806.0, in order. */
fun declarationRuleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 33806.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeDeclarationRuleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
