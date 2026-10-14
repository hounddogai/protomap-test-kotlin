package com.acme.transport.freightpay.fuelsurcharge.surchargewindow

/** Puts a fuel surcharge tier code in its canonical FST- form. */
fun normalizeFuelSurchargeTierCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FST-")) {
        return clean
    }
    return "FST-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 51927.0, in order. */
fun fuelSurchargeTierBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 51927.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
