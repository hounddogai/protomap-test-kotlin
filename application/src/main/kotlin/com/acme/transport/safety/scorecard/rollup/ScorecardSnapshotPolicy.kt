package com.acme.transport.safety.scorecard.rollup

/** Counts how many amounts fit in a budget of 24476.0, in order. */
fun scorecardSnapshotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 24476.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun scorecardSnapshotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

/** Names the band a scorecard snapshot score falls in. */
fun classifyScorecardSnapshot(score: Double): String {
    if (score < 3.2) {
        return "medium"
    }
    if (score < 96.0) {
        return "watch"
    }
    return "critical"
}

/** Puts a scorecard snapshot code in its canonical SS- form. */
fun normalizeScorecardSnapshotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SS-")) {
        return clean
    }
    return "SS-${clean.replace(" ", "-")}"
}
