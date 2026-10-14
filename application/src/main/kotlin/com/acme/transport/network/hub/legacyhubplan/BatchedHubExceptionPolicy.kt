package com.acme.transport.network.hub.legacyhubplan

/** Counts how many amounts fit in a budget of 46089.0, in order. */
fun batchedHubExceptionBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 46089.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun batchedHubExceptionSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

/** Puts a batched hub exception code in its canonical BHE- form. */
fun normalizeBatchedHubExceptionCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BHE-")) {
        return clean
    }
    return "BHE-${clean.replace(" ", "-")}"
}
