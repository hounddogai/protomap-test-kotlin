package com.acme.transport.crewing.roster.rosteragreement

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun cappedRosterNoticeBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Puts a capped roster notice code in its canonical CRN- form. */
fun normalizeCappedRosterNoticeCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CRN-")) {
        return clean
    }
    return "CRN-${clean.replace(" ", "-")}"
}

fun mergeCappedRosterNoticeLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a capped roster notice score falls in. */
fun classifyCappedRosterNotice(score: Double): String {
    if (score < 27.8) {
        return "elevated"
    }
    if (score < 40.0) {
        return "watch"
    }
    if (score < 83.2) {
        return "ok"
    }
    if (score < 91.6) {
        return "minimal"
    }
    return "high"
}
