package com.acme.transport.lastmile.locker.target

/** Counts how many amounts fit in a budget of 21138.0, in order. */
fun lockerSegmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 21138.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a locker segment code in its canonical LS- form. */
fun normalizeLockerSegmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LS-")) {
        return clean
    }
    return "LS-${clean.replace(" ", "-")}"
}

fun lockerSegmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

/** Names the band a locker segment score falls in. */
fun classifyLockerSegment(score: Double): String {
    if (score < 11.1) {
        return "critical"
    }
    if (score < 61.2) {
        return "medium"
    }
    if (score < 79.5) {
        return "elevated"
    }
    if (score < 83.2) {
        return "minimal"
    }
    return "ok"
}
