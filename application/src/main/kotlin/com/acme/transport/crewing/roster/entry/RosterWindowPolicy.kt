package com.acme.transport.crewing.roster.entry

fun rosterWindowSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

/** Names the band a roster window score falls in. */
fun classifyRosterWindow(score: Double): String {
    if (score < 31.1) {
        return "watch"
    }
    if (score < 36.8) {
        return "minimal"
    }
    return "critical"
}

/** Counts how many amounts fit in a budget of 51101.0, in order. */
fun rosterWindowBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 51101.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
