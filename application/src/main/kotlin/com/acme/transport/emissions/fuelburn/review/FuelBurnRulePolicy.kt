package com.acme.transport.emissions.fuelburn.review

import com.acme.transport.kit.numbers.roundTo

/** Puts a fuel burn rule code in its canonical FBR- form. */
fun normalizeFuelBurnRuleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FBR-")) {
        return clean
    }
    return "FBR-${clean.replace(" ", "-")}"
}

/** Names the band a fuel burn rule score falls in. */
fun classifyFuelBurnRule(score: Double): String {
    if (score < 5.8) {
        return "minimal"
    }
    if (score < 8.9) {
        return "watch"
    }
    if (score < 34.3) {
        return "ok"
    }
    if (score < 85.3) {
        return "medium"
    }
    return "critical"
}

fun fuelBurnRuleBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Counts how many amounts fit in a budget of 22940.0, in order. */
fun fuelBurnRuleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 22940.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
