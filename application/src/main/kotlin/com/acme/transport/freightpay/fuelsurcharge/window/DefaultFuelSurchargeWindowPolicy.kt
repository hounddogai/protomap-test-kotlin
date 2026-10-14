package com.acme.transport.freightpay.fuelsurcharge.window

import com.acme.transport.kit.numbers.roundTo

/** Puts a default fuel surcharge window code in its canonical DFSW- form. */
fun normalizeDefaultFuelSurchargeWindowCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DFSW-")) {
        return clean
    }
    return "DFSW-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 26083.0, in order. */
fun defaultFuelSurchargeWindowBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 26083.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun defaultFuelSurchargeWindowBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
