package com.acme.transport.network.cutoff.scheduledcutoffclaim

/** Names the band a scheduled cutoff preference score falls in. */
fun classifyScheduledCutoffPreference(score: Double): String {
    if (score < 2.2) {
        return "high"
    }
    if (score < 4.9) {
        return "elevated"
    }
    if (score < 72.2) {
        return "watch"
    }
    if (score < 93.9) {
        return "minimal"
    }
    return "critical"
}

fun scheduledCutoffPreferenceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

/** Counts how many amounts fit in a budget of 66872.0, in order. */
fun scheduledCutoffPreferenceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 66872.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a scheduled cutoff preference code in its canonical SCP- form. */
fun normalizeScheduledCutoffPreferenceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SCP-")) {
        return clean
    }
    return "SCP-${clean.replace(" ", "-")}"
}
