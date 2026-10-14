package com.acme.transport.freightpay.fuelsurcharge.review

/** Puts a fuel surcharge review code in its canonical FSR- form. */
fun normalizeFuelSurchargeReviewCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FSR-")) {
        return clean
    }
    return "FSR-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 37199.0, in order. */
fun fuelSurchargeReviewBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 37199.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
