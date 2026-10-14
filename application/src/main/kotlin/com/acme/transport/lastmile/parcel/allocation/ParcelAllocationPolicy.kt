package com.acme.transport.lastmile.parcel.allocation

/** Puts a parcel allocation code in its canonical PA- form. */
fun normalizeParcelAllocationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PA-")) {
        return clean
    }
    return "PA-${clean.replace(" ", "-")}"
}

fun parcelAllocationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Counts how many amounts fit in a budget of 93442.0, in order. */
fun parcelAllocationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 93442.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
