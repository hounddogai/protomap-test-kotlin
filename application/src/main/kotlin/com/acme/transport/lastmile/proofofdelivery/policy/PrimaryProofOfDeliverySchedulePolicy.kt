package com.acme.transport.lastmile.proofofdelivery.policy

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a primary proof of delivery schedule score falls in. */
fun classifyPrimaryProofOfDeliverySchedule(score: Double): String {
    if (score < 3.5) {
        return "low"
    }
    if (score < 6.5) {
        return "watch"
    }
    return "high"
}

fun primaryProofOfDeliveryScheduleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

fun mergePrimaryProofOfDeliveryScheduleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a primary proof of delivery schedule code in its canonical PPODS- form. */
fun normalizePrimaryProofOfDeliveryScheduleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PPODS-")) {
        return clean
    }
    return "PPODS-${clean.replace(" ", "-")}"
}
