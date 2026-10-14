package com.acme.transport.freightpay.fuelsurcharge.surchargepreference

import com.acme.transport.kit.numbers.roundTo

fun primaryFuelSurchargeBundleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

fun primaryFuelSurchargeBundleBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Counts how many amounts fit in a budget of 16483.0, in order. */
fun primaryFuelSurchargeBundleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 16483.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a primary fuel surcharge bundle code in its canonical PFSB- form. */
fun normalizePrimaryFuelSurchargeBundleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PFSB-")) {
        return clean
    }
    return "PFSB-${clean.replace(" ", "-")}"
}
