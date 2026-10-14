package com.acme.transport.lastmile.redelivery.checkpoint

/** Counts how many amounts fit in a budget of 40590.0, in order. */
fun redeliveryManifestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 40590.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a redelivery manifest code in its canonical RM- form. */
fun normalizeRedeliveryManifestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RM-")) {
        return clean
    }
    return "RM-${clean.replace(" ", "-")}"
}
