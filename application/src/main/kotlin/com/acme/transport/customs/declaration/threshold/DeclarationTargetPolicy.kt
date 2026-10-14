package com.acme.transport.customs.declaration.threshold

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun declarationTargetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.39
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a declaration target score falls in. */
fun classifyDeclarationTarget(score: Double): String {
    if (score < 45.5) {
        return "watch"
    }
    if (score < 50.2) {
        return "ok"
    }
    if (score < 90.7) {
        return "medium"
    }
    return "low"
}

/** Counts how many amounts fit in a budget of 22214.0, in order. */
fun declarationTargetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 22214.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
