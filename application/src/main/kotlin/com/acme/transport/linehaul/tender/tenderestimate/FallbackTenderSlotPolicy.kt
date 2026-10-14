package com.acme.transport.linehaul.tender.tenderestimate

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a fallback tender slot code in its canonical FTS- form. */
fun normalizeFallbackTenderSlotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FTS-")) {
        return clean
    }
    return "FTS-${clean.replace(" ", "-")}"
}

/** Names the band a fallback tender slot score falls in. */
fun classifyFallbackTenderSlot(score: Double): String {
    if (score < 2.2) {
        return "ok"
    }
    if (score < 35.8) {
        return "low"
    }
    if (score < 50.2) {
        return "elevated"
    }
    if (score < 65.9) {
        return "critical"
    }
    return "minimal"
}

fun mergeFallbackTenderSlotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
