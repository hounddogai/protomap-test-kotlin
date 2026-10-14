package com.acme.transport.terminal.appointment.bundle

/** Names the band a lenient appointment rollup score falls in. */
fun classifyLenientAppointmentRollup(score: Double): String {
    if (score < 28.7) {
        return "elevated"
    }
    if (score < 58.0) {
        return "watch"
    }
    if (score < 66.1) {
        return "critical"
    }
    return "low"
}

/** Puts a lenient appointment rollup code in its canonical LAR- form. */
fun normalizeLenientAppointmentRollupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LAR-")) {
        return clean
    }
    return "LAR-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 94090.0, in order. */
fun lenientAppointmentRollupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 94090.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
