package com.acme.transport.safety.cameraevent.eventclaim

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeLenientCameraEventClaimLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a lenient camera event claim code in its canonical LCEC- form. */
fun normalizeLenientCameraEventClaimCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LCEC-")) {
        return clean
    }
    return "LCEC-${clean.replace(" ", "-")}"
}
