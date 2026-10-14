package com.acme.transport.customs.inspection.weightedinspectionplan

fun globalInspectionPlanSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

/** Puts a global inspection plan code in its canonical GIP- form. */
fun normalizeGlobalInspectionPlanCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GIP-")) {
        return clean
    }
    return "GIP-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 44644.0, in order. */
fun globalInspectionPlanBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 44644.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
