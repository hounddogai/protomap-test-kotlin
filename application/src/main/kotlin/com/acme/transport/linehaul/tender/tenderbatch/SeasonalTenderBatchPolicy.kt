package com.acme.transport.linehaul.tender.tenderbatch

import com.acme.transport.kit.numbers.roundTo

/** Puts a seasonal tender batch code in its canonical STB- form. */
fun normalizeSeasonalTenderBatchCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("STB-")) {
        return clean
    }
    return "STB-${clean.replace(" ", "-")}"
}

fun seasonalTenderBatchBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Counts how many amounts fit in a budget of 24973.0, in order. */
fun seasonalTenderBatchBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 24973.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
