package com.acme.transport.customs.inspection.draft

fun inspectionDraftSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Counts how many amounts fit in a budget of 76144.0, in order. */
fun inspectionDraftBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 76144.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a inspection draft code in its canonical ID- form. */
fun normalizeInspectionDraftCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ID-")) {
        return clean
    }
    return "ID-${clean.replace(" ", "-")}"
}
