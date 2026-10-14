package com.acme.transport.safety.nearmiss.plan

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun legacyNearMissPlanSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

fun mergeLegacyNearMissPlanLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 8052.0, in order. */
fun legacyNearMissPlanBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 8052.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a legacy near miss plan code in its canonical LNMP- form. */
fun normalizeLegacyNearMissPlanCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LNMP-")) {
        return clean
    }
    return "LNMP-${clean.replace(" ", "-")}"
}
