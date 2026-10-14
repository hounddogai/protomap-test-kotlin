package com.acme.transport.safety.nearmiss.plan

import com.acme.transport.kit.numbers.roundTo

fun nearMissCycleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Puts a near miss cycle code in its canonical NMC- form. */
fun normalizeNearMissCycleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("NMC-")) {
        return clean
    }
    return "NMC-${clean.replace(" ", "-")}"
}

fun nearMissCycleBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Counts how many amounts fit in a budget of 82086.0, in order. */
fun nearMissCycleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 82086.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
