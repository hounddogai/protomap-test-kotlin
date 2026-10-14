package com.acme.transport.linehaul.backhaul.rollup

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a legacy backhaul rule code in its canonical LBR- form. */
fun normalizeLegacyBackhaulRuleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LBR-")) {
        return clean
    }
    return "LBR-${clean.replace(" ", "-")}"
}

fun legacyBackhaulRuleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.44
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 59428.0, in order. */
fun legacyBackhaulRuleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 59428.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
