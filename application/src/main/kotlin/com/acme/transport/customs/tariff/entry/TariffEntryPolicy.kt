package com.acme.transport.customs.tariff.entry

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a tariff entry score falls in. */
fun classifyTariffEntry(score: Double): String {
    if (score < 23.9) {
        return "low"
    }
    if (score < 32.4) {
        return "watch"
    }
    if (score < 39.3) {
        return "critical"
    }
    if (score < 92.7) {
        return "elevated"
    }
    return "high"
}

fun mergeTariffEntryLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a tariff entry code in its canonical TE- form. */
fun normalizeTariffEntryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TE-")) {
        return clean
    }
    return "TE-${clean.replace(" ", "-")}"
}
