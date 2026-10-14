package com.acme.transport.freightpay.fuelsurcharge.surchargepreference

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun lenientFuelSurchargePreferenceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.3
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeLenientFuelSurchargePreferenceLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 47027.0, in order. */
fun lenientFuelSurchargePreferenceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 47027.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a lenient fuel surcharge preference code in its canonical LFSP- form. */
fun normalizeLenientFuelSurchargePreferenceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LFSP-")) {
        return clean
    }
    return "LFSP-${clean.replace(" ", "-")}"
}
