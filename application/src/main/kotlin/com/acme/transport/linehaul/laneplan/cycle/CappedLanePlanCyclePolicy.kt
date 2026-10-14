package com.acme.transport.linehaul.laneplan.cycle

import com.acme.transport.kit.numbers.roundTo

/** Puts a capped lane plan cycle code in its canonical CLPC- form. */
fun normalizeCappedLanePlanCycleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CLPC-")) {
        return clean
    }
    return "CLPC-${clean.replace(" ", "-")}"
}

fun cappedLanePlanCycleBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Names the band a capped lane plan cycle score falls in. */
fun classifyCappedLanePlanCycle(score: Double): String {
    if (score < 16.0) {
        return "ok"
    }
    if (score < 24.4) {
        return "low"
    }
    return "high"
}

/** Counts how many amounts fit in a budget of 82459.0, in order. */
fun cappedLanePlanCycleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 82459.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
