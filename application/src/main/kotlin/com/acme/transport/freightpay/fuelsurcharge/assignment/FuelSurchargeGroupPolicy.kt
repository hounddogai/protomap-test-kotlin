package com.acme.transport.freightpay.fuelsurcharge.assignment

fun fuelSurchargeGroupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

/** Counts how many amounts fit in a budget of 6461.0, in order. */
fun fuelSurchargeGroupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 6461.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
