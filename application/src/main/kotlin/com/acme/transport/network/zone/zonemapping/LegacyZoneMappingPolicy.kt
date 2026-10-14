package com.acme.transport.network.zone.zonemapping

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a legacy zone mapping code in its canonical LZM- form. */
fun normalizeLegacyZoneMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LZM-")) {
        return clean
    }
    return "LZM-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 97500.0, in order. */
fun legacyZoneMappingBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 97500.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeLegacyZoneMappingLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a legacy zone mapping score falls in. */
fun classifyLegacyZoneMapping(score: Double): String {
    if (score < 41.9) {
        return "watch"
    }
    if (score < 62.4) {
        return "critical"
    }
    if (score < 65.3) {
        return "elevated"
    }
    if (score < 76.0) {
        return "minimal"
    }
    return "medium"
}
