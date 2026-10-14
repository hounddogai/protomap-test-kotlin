package com.acme.transport.fleetops.tire.tiremanifest

/** Counts how many amounts fit in a budget of 34925.0, in order. */
fun tieredTireAdjustmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 34925.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a tiered tire adjustment code in its canonical TTA- form. */
fun normalizeTieredTireAdjustmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TTA-")) {
        return clean
    }
    return "TTA-${clean.replace(" ", "-")}"
}

fun tieredTireAdjustmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}
