package com.acme.transport.emissions.evcharge.chargepreference

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Puts a capped ev charge preference code in its canonical CECP- form. */
fun normalizeCappedEvChargePreferenceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CECP-")) {
        return clean
    }
    return "CECP-${clean.replace(" ", "-")}"
}

/** Names the band a capped ev charge preference score falls in. */
fun classifyCappedEvChargePreference(score: Double): String {
    if (score < 18.1) {
        return "low"
    }
    if (score < 85.6) {
        return "minimal"
    }
    return "medium"
}

fun mergeCappedEvChargePreferenceLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun cappedEvChargePreferenceBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
