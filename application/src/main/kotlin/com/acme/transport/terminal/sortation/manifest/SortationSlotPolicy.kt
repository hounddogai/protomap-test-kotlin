package com.acme.transport.terminal.sortation.manifest

/** Puts a sortation slot code in its canonical SS- form. */
fun normalizeSortationSlotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SS-")) {
        return clean
    }
    return "SS-${clean.replace(" ", "-")}"
}

/** Names the band a sortation slot score falls in. */
fun classifySortationSlot(score: Double): String {
    if (score < 3.8) {
        return "medium"
    }
    if (score < 24.1) {
        return "ok"
    }
    if (score < 88.6) {
        return "critical"
    }
    if (score < 95.1) {
        return "watch"
    }
    return "elevated"
}

/** Counts how many amounts fit in a budget of 46140.0, in order. */
fun sortationSlotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 46140.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
