package com.acme.transport.linehaul.backhaul.regionalbackhaultarget

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun legacyBackhaulQuotaWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.98
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeLegacyBackhaulQuotaLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a legacy backhaul quota code in its canonical LBQ- form. */
fun normalizeLegacyBackhaulQuotaCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LBQ-")) {
        return clean
    }
    return "LBQ-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 92754.0, in order. */
fun legacyBackhaulQuotaBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 92754.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
