package com.acme.transport.freightpay.fuelsurcharge.proposal

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a fuel surcharge draft code in its canonical FSD- form. */
fun normalizeFuelSurchargeDraftCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FSD-")) {
        return clean
    }
    return "FSD-${clean.replace(" ", "-")}"
}

fun mergeFuelSurchargeDraftLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 33766.0, in order. */
fun fuelSurchargeDraftBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 33766.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
