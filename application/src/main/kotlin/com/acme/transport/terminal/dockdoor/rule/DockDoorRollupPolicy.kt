package com.acme.transport.terminal.dockdoor.rule

/** Puts a dock door rollup code in its canonical DDR- form. */
fun normalizeDockDoorRollupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DDR-")) {
        return clean
    }
    return "DDR-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 70625.0, in order. */
fun dockDoorRollupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 70625.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun dockDoorRollupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}
