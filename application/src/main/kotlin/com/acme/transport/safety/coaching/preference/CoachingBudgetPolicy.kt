package com.acme.transport.safety.coaching.preference

/** Names the band a coaching budget score falls in. */
fun classifyCoachingBudget(score: Double): String {
    if (score < 2.3) {
        return "critical"
    }
    if (score < 27.2) {
        return "watch"
    }
    if (score < 87.5) {
        return "medium"
    }
    return "low"
}

fun coachingBudgetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Counts how many amounts fit in a budget of 26007.0, in order. */
fun coachingBudgetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 26007.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a coaching budget code in its canonical CB- form. */
fun normalizeCoachingBudgetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CB-")) {
        return clean
    }
    return "CB-${clean.replace(" ", "-")}"
}
