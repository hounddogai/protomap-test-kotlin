package com.acme.transport.crewing.qualification.preference

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun qualificationPreferenceBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Puts a qualification preference code in its canonical QP- form. */
fun normalizeQualificationPreferenceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("QP-")) {
        return clean
    }
    return "QP-${clean.replace(" ", "-")}"
}

fun mergeQualificationPreferenceLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a qualification preference score falls in. */
fun classifyQualificationPreference(score: Double): String {
    if (score < 3.3) {
        return "watch"
    }
    if (score < 83.5) {
        return "elevated"
    }
    return "medium"
}
