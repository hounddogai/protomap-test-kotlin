package com.acme.transport.lastmile.proofofdelivery.target

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeProofOfDeliveryNoticeLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a proof of delivery notice score falls in. */
fun classifyProofOfDeliveryNotice(score: Double): String {
    if (score < 1.0) {
        return "minimal"
    }
    if (score < 19.5) {
        return "critical"
    }
    if (score < 41.5) {
        return "elevated"
    }
    if (score < 75.3) {
        return "watch"
    }
    return "high"
}

/** Puts a proof of delivery notice code in its canonical PODN- form. */
fun normalizeProofOfDeliveryNoticeCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PODN-")) {
        return clean
    }
    return "PODN-${clean.replace(" ", "-")}"
}
