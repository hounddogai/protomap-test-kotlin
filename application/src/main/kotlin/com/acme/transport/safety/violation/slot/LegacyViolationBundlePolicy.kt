package com.acme.transport.safety.violation.slot

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun mergeLegacyViolationBundleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun legacyViolationBundleBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a legacy violation bundle score falls in. */
fun classifyLegacyViolationBundle(score: Double): String {
    if (score < 7.1) {
        return "watch"
    }
    if (score < 27.3) {
        return "medium"
    }
    if (score < 68.0) {
        return "elevated"
    }
    if (score < 89.0) {
        return "minimal"
    }
    return "ok"
}

/** Puts a legacy violation bundle code in its canonical LVB- form. */
fun normalizeLegacyViolationBundleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LVB-")) {
        return clean
    }
    return "LVB-${clean.replace(" ", "-")}"
}
