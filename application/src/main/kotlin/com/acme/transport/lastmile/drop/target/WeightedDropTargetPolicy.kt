package com.acme.transport.lastmile.drop.target

/** Puts a weighted drop target code in its canonical WDT- form. */
fun normalizeWeightedDropTargetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WDT-")) {
        return clean
    }
    return "WDT-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 16092.0, in order. */
fun weightedDropTargetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 16092.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun weightedDropTargetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}
