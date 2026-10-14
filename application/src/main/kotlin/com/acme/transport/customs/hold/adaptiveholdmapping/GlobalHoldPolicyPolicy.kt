package com.acme.transport.customs.hold.adaptiveholdmapping

/** Counts how many amounts fit in a budget of 9945.0, in order. */
fun globalHoldPolicyBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 9945.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun globalHoldPolicySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

/** Puts a global hold policy code in its canonical GHP- form. */
fun normalizeGlobalHoldPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GHP-")) {
        return clean
    }
    return "GHP-${clean.replace(" ", "-")}"
}
