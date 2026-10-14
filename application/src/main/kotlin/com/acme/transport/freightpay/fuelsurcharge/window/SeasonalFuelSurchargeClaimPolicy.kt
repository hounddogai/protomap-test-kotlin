package com.acme.transport.freightpay.fuelsurcharge.window

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a seasonal fuel surcharge claim code in its canonical SFSC- form. */
fun normalizeSeasonalFuelSurchargeClaimCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SFSC-")) {
        return clean
    }
    return "SFSC-${clean.replace(" ", "-")}"
}

fun mergeSeasonalFuelSurchargeClaimLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 24783.0, in order. */
fun seasonalFuelSurchargeClaimBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 24783.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun seasonalFuelSurchargeClaimSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}
