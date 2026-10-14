package com.acme.transport.safety.coaching.segment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a capped coaching digest code in its canonical CCD- form. */
fun normalizeCappedCoachingDigestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CCD-")) {
        return clean
    }
    return "CCD-${clean.replace(" ", "-")}"
}

fun mergeCappedCoachingDigestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a capped coaching digest score falls in. */
fun classifyCappedCoachingDigest(score: Double): String {
    if (score < 40.8) {
        return "medium"
    }
    if (score < 45.9) {
        return "high"
    }
    return "minimal"
}
