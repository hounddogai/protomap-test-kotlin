package com.acme.transport.emissions.emissionfactor.manifest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a emission factor slot score falls in. */
fun classifyEmissionFactorSlot(score: Double): String {
    if (score < 26.7) {
        return "elevated"
    }
    if (score < 28.6) {
        return "critical"
    }
    if (score < 72.3) {
        return "medium"
    }
    if (score < 77.0) {
        return "ok"
    }
    return "minimal"
}

/** Puts a emission factor slot code in its canonical EFS- form. */
fun normalizeEmissionFactorSlotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("EFS-")) {
        return clean
    }
    return "EFS-${clean.replace(" ", "-")}"
}

fun mergeEmissionFactorSlotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
