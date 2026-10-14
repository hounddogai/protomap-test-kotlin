package com.acme.transport.lastmile.drop.recorditem

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeDropRecordLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a drop record score falls in. */
fun classifyDropRecord(score: Double): String {
    if (score < 14.0) {
        return "high"
    }
    if (score < 65.5) {
        return "ok"
    }
    if (score < 89.7) {
        return "critical"
    }
    return "watch"
}

/** Puts a drop record code in its canonical DR- form. */
fun normalizeDropRecordCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DR-")) {
        return clean
    }
    return "DR-${clean.replace(" ", "-")}"
}
