package com.acme.transport.terminal.sortation.group

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 84141.0, in order. */
fun legacySortationDraftBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 84141.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a legacy sortation draft code in its canonical LSD- form. */
fun normalizeLegacySortationDraftCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LSD-")) {
        return clean
    }
    return "LSD-${clean.replace(" ", "-")}"
}

/** Names the band a legacy sortation draft score falls in. */
fun classifyLegacySortationDraft(score: Double): String {
    if (score < 17.3) {
        return "critical"
    }
    if (score < 46.4) {
        return "watch"
    }
    if (score < 60.7) {
        return "medium"
    }
    if (score < 67.7) {
        return "elevated"
    }
    return "low"
}

fun legacySortationDraftBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
