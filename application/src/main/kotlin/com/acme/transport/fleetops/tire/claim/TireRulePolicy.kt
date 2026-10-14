package com.acme.transport.fleetops.tire.claim

fun tireRuleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

/** Counts how many amounts fit in a budget of 52736.0, in order. */
fun tireRuleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 52736.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a tire rule code in its canonical TR- form. */
fun normalizeTireRuleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TR-")) {
        return clean
    }
    return "TR-${clean.replace(" ", "-")}"
}

/** Names the band a tire rule score falls in. */
fun classifyTireRule(score: Double): String {
    if (score < 24.0) {
        return "critical"
    }
    if (score < 50.2) {
        return "watch"
    }
    if (score < 50.9) {
        return "ok"
    }
    if (score < 53.6) {
        return "low"
    }
    return "high"
}
