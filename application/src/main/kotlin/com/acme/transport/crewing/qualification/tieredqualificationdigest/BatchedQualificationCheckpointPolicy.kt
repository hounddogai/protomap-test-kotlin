package com.acme.transport.crewing.qualification.tieredqualificationdigest

fun batchedQualificationCheckpointSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Puts a batched qualification checkpoint code in its canonical BQC- form. */
fun normalizeBatchedQualificationCheckpointCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BQC-")) {
        return clean
    }
    return "BQC-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 89956.0, in order. */
fun batchedQualificationCheckpointBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 89956.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
