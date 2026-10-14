package com.acme.transport.crewing.qualification.qualificationpreference

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Names the band a global qualification preference score falls in. */
fun classifyGlobalQualificationPreference(score: Double): String {
    if (score < 7.7) {
        return "watch"
    }
    if (score < 48.5) {
        return "medium"
    }
    if (score < 81.4) {
        return "ok"
    }
    return "high"
}

fun mergeGlobalQualificationPreferenceLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a global qualification preference code in its canonical GQP- form. */
fun normalizeGlobalQualificationPreferenceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GQP-")) {
        return clean
    }
    return "GQP-${clean.replace(" ", "-")}"
}

fun globalQualificationPreferenceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.61
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
