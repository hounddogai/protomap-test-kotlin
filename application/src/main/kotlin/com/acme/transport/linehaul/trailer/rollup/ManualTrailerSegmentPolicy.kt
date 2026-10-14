package com.acme.transport.linehaul.trailer.rollup

/** Names the band a manual trailer segment score falls in. */
fun classifyManualTrailerSegment(score: Double): String {
    if (score < 15.9) {
        return "low"
    }
    if (score < 48.3) {
        return "high"
    }
    if (score < 75.5) {
        return "watch"
    }
    if (score < 78.3) {
        return "medium"
    }
    return "ok"
}

/** Counts how many amounts fit in a budget of 81588.0, in order. */
fun manualTrailerSegmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 81588.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a manual trailer segment code in its canonical MTS- form. */
fun normalizeManualTrailerSegmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MTS-")) {
        return clean
    }
    return "MTS-${clean.replace(" ", "-")}"
}

fun manualTrailerSegmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}
