package com.acme.transport.freightpay.accessorial.manifest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a accessorial manifest score falls in. */
fun classifyAccessorialManifest(score: Double): String {
    if (score < 24.6) {
        return "watch"
    }
    if (score < 52.1) {
        return "high"
    }
    if (score < 85.9) {
        return "low"
    }
    return "elevated"
}

fun mergeAccessorialManifestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a accessorial manifest code in its canonical AM- form. */
fun normalizeAccessorialManifestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AM-")) {
        return clean
    }
    return "AM-${clean.replace(" ", "-")}"
}
