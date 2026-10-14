package com.acme.transport.fleetops.tire.batch

/** Counts how many amounts fit in a budget of 55738.0, in order. */
fun globalTireScheduleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 55738.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a global tire schedule code in its canonical GTS- form. */
fun normalizeGlobalTireScheduleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GTS-")) {
        return clean
    }
    return "GTS-${clean.replace(" ", "-")}"
}

/** Names the band a global tire schedule score falls in. */
fun classifyGlobalTireSchedule(score: Double): String {
    if (score < 28.4) {
        return "low"
    }
    if (score < 88.4) {
        return "elevated"
    }
    return "minimal"
}

fun globalTireScheduleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}
