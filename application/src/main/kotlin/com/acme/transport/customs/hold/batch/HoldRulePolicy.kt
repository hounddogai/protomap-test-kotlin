package com.acme.transport.customs.hold.batch

import com.acme.transport.kit.numbers.roundTo

fun holdRuleBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Puts a hold rule code in its canonical HR- form. */
fun normalizeHoldRuleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("HR-")) {
        return clean
    }
    return "HR-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 94789.0, in order. */
fun holdRuleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 94789.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a hold rule score falls in. */
fun classifyHoldRule(score: Double): String {
    if (score < 23.1) {
        return "minimal"
    }
    if (score < 75.1) {
        return "ok"
    }
    return "low"
}
