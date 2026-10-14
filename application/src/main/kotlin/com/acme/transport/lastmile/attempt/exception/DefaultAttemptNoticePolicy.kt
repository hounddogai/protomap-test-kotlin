package com.acme.transport.lastmile.attempt.exception

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeDefaultAttemptNoticeLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a default attempt notice code in its canonical DAN- form. */
fun normalizeDefaultAttemptNoticeCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DAN-")) {
        return clean
    }
    return "DAN-${clean.replace(" ", "-")}"
}

/** Names the band a default attempt notice score falls in. */
fun classifyDefaultAttemptNotice(score: Double): String {
    if (score < 21.8) {
        return "elevated"
    }
    if (score < 32.6) {
        return "medium"
    }
    if (score < 91.9) {
        return "minimal"
    }
    return "critical"
}
