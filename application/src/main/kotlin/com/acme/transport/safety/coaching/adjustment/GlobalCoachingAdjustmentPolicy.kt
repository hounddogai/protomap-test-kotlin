package com.acme.transport.safety.coaching.adjustment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a global coaching adjustment score falls in. */
fun classifyGlobalCoachingAdjustment(score: Double): String {
    if (score < 33.7) {
        return "ok"
    }
    if (score < 36.7) {
        return "minimal"
    }
    if (score < 56.9) {
        return "low"
    }
    if (score < 90.6) {
        return "high"
    }
    return "watch"
}

/** Puts a global coaching adjustment code in its canonical GCA- form. */
fun normalizeGlobalCoachingAdjustmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GCA-")) {
        return clean
    }
    return "GCA-${clean.replace(" ", "-")}"
}

fun mergeGlobalCoachingAdjustmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
