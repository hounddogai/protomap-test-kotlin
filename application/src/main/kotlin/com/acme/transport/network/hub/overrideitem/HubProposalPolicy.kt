package com.acme.transport.network.hub.overrideitem

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeHubProposalLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a hub proposal score falls in. */
fun classifyHubProposal(score: Double): String {
    if (score < 24.5) {
        return "elevated"
    }
    if (score < 75.9) {
        return "medium"
    }
    if (score < 77.2) {
        return "high"
    }
    return "ok"
}

/** Puts a hub proposal code in its canonical HP- form. */
fun normalizeHubProposalCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("HP-")) {
        return clean
    }
    return "HP-${clean.replace(" ", "-")}"
}
