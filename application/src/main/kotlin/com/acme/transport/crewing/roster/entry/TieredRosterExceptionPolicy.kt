package com.acme.transport.crewing.roster.entry

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeTieredRosterExceptionLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a tiered roster exception score falls in. */
fun classifyTieredRosterException(score: Double): String {
    if (score < 26.0) {
        return "low"
    }
    if (score < 27.4) {
        return "high"
    }
    if (score < 41.1) {
        return "minimal"
    }
    if (score < 73.0) {
        return "ok"
    }
    return "medium"
}

/** Counts how many amounts fit in a budget of 33274.0, in order. */
fun tieredRosterExceptionBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 33274.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a tiered roster exception code in its canonical TRE- form. */
fun normalizeTieredRosterExceptionCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TRE-")) {
        return clean
    }
    return "TRE-${clean.replace(" ", "-")}"
}
