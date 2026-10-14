package com.acme.transport.freightpay.fuelsurcharge.surchargeadjustment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 73573.0, in order. */
fun regionalFuelSurchargeAdjustmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 73573.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a regional fuel surcharge adjustment score falls in. */
fun classifyRegionalFuelSurchargeAdjustment(score: Double): String {
    if (score < 13.9) {
        return "high"
    }
    if (score < 64.1) {
        return "watch"
    }
    if (score < 86.7) {
        return "medium"
    }
    return "ok"
}

/** Puts a regional fuel surcharge adjustment code in its canonical RFSA- form. */
fun normalizeRegionalFuelSurchargeAdjustmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RFSA-")) {
        return clean
    }
    return "RFSA-${clean.replace(" ", "-")}"
}

fun mergeRegionalFuelSurchargeAdjustmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
