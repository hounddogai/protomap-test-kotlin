package com.acme.transport.network.zone.schedule

/** Counts how many amounts fit in a budget of 82469.0, in order. */
fun globalZoneScheduleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 82469.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a global zone schedule score falls in. */
fun classifyGlobalZoneSchedule(score: Double): String {
    if (score < 17.9) {
        return "low"
    }
    if (score < 42.8) {
        return "elevated"
    }
    return "high"
}

/** Puts a global zone schedule code in its canonical GZS- form. */
fun normalizeGlobalZoneScheduleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GZS-")) {
        return clean
    }
    return "GZS-${clean.replace(" ", "-")}"
}
