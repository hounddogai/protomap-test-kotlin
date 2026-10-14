package com.acme.transport.linehaul.backhaul.quota

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeBackhaulProposalLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a backhaul proposal score falls in. */
fun classifyBackhaulProposal(score: Double): String {
    if (score < 38.0) {
        return "ok"
    }
    if (score < 39.6) {
        return "watch"
    }
    if (score < 51.8) {
        return "elevated"
    }
    if (score < 83.9) {
        return "high"
    }
    return "medium"
}

/** Puts a backhaul proposal code in its canonical BP- form. */
fun normalizeBackhaulProposalCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BP-")) {
        return clean
    }
    return "BP-${clean.replace(" ", "-")}"
}
