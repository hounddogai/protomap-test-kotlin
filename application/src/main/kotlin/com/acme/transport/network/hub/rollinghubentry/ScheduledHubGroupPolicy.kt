package com.acme.transport.network.hub.rollinghubentry

import com.acme.transport.kit.numbers.roundTo

/** Puts a scheduled hub group code in its canonical SHG- form. */
fun normalizeScheduledHubGroupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SHG-")) {
        return clean
    }
    return "SHG-${clean.replace(" ", "-")}"
}

fun scheduledHubGroupBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Counts how many amounts fit in a budget of 84747.0, in order. */
fun scheduledHubGroupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 84747.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a scheduled hub group score falls in. */
fun classifyScheduledHubGroup(score: Double): String {
    if (score < 42.2) {
        return "watch"
    }
    if (score < 43.1) {
        return "medium"
    }
    if (score < 43.9) {
        return "elevated"
    }
    if (score < 83.6) {
        return "ok"
    }
    return "low"
}
