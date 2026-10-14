package com.acme.transport.lastmile.proofofdelivery.review

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a proof of delivery policy code in its canonical PODP- form. */
fun normalizeProofOfDeliveryPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PODP-")) {
        return clean
    }
    return "PODP-${clean.replace(" ", "-")}"
}

fun mergeProofOfDeliveryPolicyLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
