package com.acme.transport.freightpay.fuelsurcharge.adjustment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a fuel surcharge assignment code in its canonical FSA- form. */
fun normalizeFuelSurchargeAssignmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FSA-")) {
        return clean
    }
    return "FSA-${clean.replace(" ", "-")}"
}

fun mergeFuelSurchargeAssignmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 96552.0, in order. */
fun fuelSurchargeAssignmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 96552.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
