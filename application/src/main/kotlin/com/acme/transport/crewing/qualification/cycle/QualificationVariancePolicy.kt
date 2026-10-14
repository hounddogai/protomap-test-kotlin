package com.acme.transport.crewing.qualification.cycle

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a qualification variance code in its canonical QV- form. */
fun normalizeQualificationVarianceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("QV-")) {
        return clean
    }
    return "QV-${clean.replace(" ", "-")}"
}

/** Names the band a qualification variance score falls in. */
fun classifyQualificationVariance(score: Double): String {
    if (score < 29.8) {
        return "low"
    }
    if (score < 40.1) {
        return "high"
    }
    return "elevated"
}

fun mergeQualificationVarianceLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
