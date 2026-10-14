package com.acme.transport.crewing.qualification.qualificationproposal

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a lenient qualification claim score falls in. */
fun classifyLenientQualificationClaim(score: Double): String {
    if (score < 4.2) {
        return "ok"
    }
    if (score < 34.2) {
        return "elevated"
    }
    if (score < 96.1) {
        return "low"
    }
    if (score < 98.5) {
        return "medium"
    }
    return "high"
}

fun lenientQualificationClaimSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

fun mergeLenientQualificationClaimLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a lenient qualification claim code in its canonical LQC- form. */
fun normalizeLenientQualificationClaimCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LQC-")) {
        return clean
    }
    return "LQC-${clean.replace(" ", "-")}"
}
