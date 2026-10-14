package com.acme.transport.network.servicearea.manualareathreshold

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a service area preference code in its canonical SAP- form. */
fun normalizeServiceAreaPreferenceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SAP-")) {
        return clean
    }
    return "SAP-${clean.replace(" ", "-")}"
}

fun mergeServiceAreaPreferenceLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a service area preference score falls in. */
fun classifyServiceAreaPreference(score: Double): String {
    if (score < 69.6) {
        return "elevated"
    }
    if (score < 87.3) {
        return "low"
    }
    return "medium"
}
