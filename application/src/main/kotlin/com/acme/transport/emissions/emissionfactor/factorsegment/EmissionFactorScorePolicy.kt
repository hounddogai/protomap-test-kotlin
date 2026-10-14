package com.acme.transport.emissions.emissionfactor.factorsegment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a emission factor score code in its canonical EFS- form. */
fun normalizeEmissionFactorScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("EFS-")) {
        return clean
    }
    return "EFS-${clean.replace(" ", "-")}"
}

/** Names the band a emission factor score score falls in. */
fun classifyEmissionFactorScore(score: Double): String {
    if (score < 5.0) {
        return "low"
    }
    if (score < 11.0) {
        return "ok"
    }
    if (score < 42.7) {
        return "medium"
    }
    if (score < 74.2) {
        return "critical"
    }
    return "high"
}

fun mergeEmissionFactorScoreLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
