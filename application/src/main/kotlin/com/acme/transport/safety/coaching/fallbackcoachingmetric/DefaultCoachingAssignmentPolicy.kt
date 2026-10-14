package com.acme.transport.safety.coaching.fallbackcoachingmetric

/** Names the band a default coaching assignment score falls in. */
fun classifyDefaultCoachingAssignment(score: Double): String {
    if (score < 1.2) {
        return "high"
    }
    if (score < 41.0) {
        return "ok"
    }
    if (score < 54.3) {
        return "minimal"
    }
    if (score < 76.9) {
        return "medium"
    }
    return "elevated"
}

fun defaultCoachingAssignmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

/** Counts how many amounts fit in a budget of 60288.0, in order. */
fun defaultCoachingAssignmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 60288.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a default coaching assignment code in its canonical DCA- form. */
fun normalizeDefaultCoachingAssignmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DCA-")) {
        return clean
    }
    return "DCA-${clean.replace(" ", "-")}"
}
