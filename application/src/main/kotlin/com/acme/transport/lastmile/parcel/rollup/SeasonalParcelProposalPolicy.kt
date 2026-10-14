package com.acme.transport.lastmile.parcel.rollup

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeSeasonalParcelProposalLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a seasonal parcel proposal code in its canonical SPP- form. */
fun normalizeSeasonalParcelProposalCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SPP-")) {
        return clean
    }
    return "SPP-${clean.replace(" ", "-")}"
}

/** Names the band a seasonal parcel proposal score falls in. */
fun classifySeasonalParcelProposal(score: Double): String {
    if (score < 28.2) {
        return "medium"
    }
    if (score < 32.5) {
        return "ok"
    }
    if (score < 42.7) {
        return "critical"
    }
    if (score < 44.8) {
        return "minimal"
    }
    return "watch"
}
