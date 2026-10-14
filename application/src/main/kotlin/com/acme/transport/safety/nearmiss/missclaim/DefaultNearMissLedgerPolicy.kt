package com.acme.transport.safety.nearmiss.missclaim

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Puts a default near miss ledger code in its canonical DNML- form. */
fun normalizeDefaultNearMissLedgerCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DNML-")) {
        return clean
    }
    return "DNML-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 88649.0, in order. */
fun defaultNearMissLedgerBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 88649.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeDefaultNearMissLedgerLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun defaultNearMissLedgerWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.43
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
