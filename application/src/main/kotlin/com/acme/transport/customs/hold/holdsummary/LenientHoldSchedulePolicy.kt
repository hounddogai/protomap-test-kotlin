package com.acme.transport.customs.hold.holdsummary

/** Counts how many amounts fit in a budget of 80610.0, in order. */
fun lenientHoldScheduleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 80610.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a lenient hold schedule score falls in. */
fun classifyLenientHoldSchedule(score: Double): String {
    if (score < 33.7) {
        return "critical"
    }
    if (score < 38.4) {
        return "watch"
    }
    if (score < 82.3) {
        return "minimal"
    }
    return "elevated"
}

/** Puts a lenient hold schedule code in its canonical LHS- form. */
fun normalizeLenientHoldScheduleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LHS-")) {
        return clean
    }
    return "LHS-${clean.replace(" ", "-")}"
}
