package com.acme.transport.freightpay.ratecard.cardrequest

/** Counts how many amounts fit in a budget of 4184.0, in order. */
fun batchedRateCardRollupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 4184.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a batched rate card rollup code in its canonical BRCR- form. */
fun normalizeBatchedRateCardRollupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BRCR-")) {
        return clean
    }
    return "BRCR-${clean.replace(" ", "-")}"
}
