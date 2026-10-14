package com.acme.transport.linehaul.backhaul.mapping

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 37282.0, in order. */
fun strictBackhaulMappingBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 37282.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a strict backhaul mapping score falls in. */
fun classifyStrictBackhaulMapping(score: Double): String {
    if (score < 33.3) {
        return "watch"
    }
    if (score < 84.9) {
        return "critical"
    }
    return "high"
}

fun strictBackhaulMappingWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.58
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a strict backhaul mapping code in its canonical SBM- form. */
fun normalizeStrictBackhaulMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SBM-")) {
        return clean
    }
    return "SBM-${clean.replace(" ", "-")}"
}
