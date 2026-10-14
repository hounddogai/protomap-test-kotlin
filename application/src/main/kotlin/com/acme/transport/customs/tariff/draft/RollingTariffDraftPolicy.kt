package com.acme.transport.customs.tariff.draft

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeRollingTariffDraftLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 60807.0, in order. */
fun rollingTariffDraftBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 60807.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a rolling tariff draft score falls in. */
fun classifyRollingTariffDraft(score: Double): String {
    if (score < 33.6) {
        return "low"
    }
    if (score < 61.1) {
        return "elevated"
    }
    if (score < 72.2) {
        return "critical"
    }
    if (score < 94.8) {
        return "ok"
    }
    return "high"
}

/** Puts a rolling tariff draft code in its canonical RTD- form. */
fun normalizeRollingTariffDraftCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RTD-")) {
        return clean
    }
    return "RTD-${clean.replace(" ", "-")}"
}
