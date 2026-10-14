package com.acme.transport.customs.broker.agreement

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeFallbackBrokerCheckpointLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a fallback broker checkpoint score falls in. */
fun classifyFallbackBrokerCheckpoint(score: Double): String {
    if (score < 45.4) {
        return "high"
    }
    if (score < 56.2) {
        return "minimal"
    }
    return "low"
}

/** Puts a fallback broker checkpoint code in its canonical FBC- form. */
fun normalizeFallbackBrokerCheckpointCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FBC-")) {
        return clean
    }
    return "FBC-${clean.replace(" ", "-")}"
}
