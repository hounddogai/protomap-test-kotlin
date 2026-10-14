package com.acme.transport.safety.cameraevent.ledger

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a camera event estimate code in its canonical CEE- form. */
fun normalizeCameraEventEstimateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CEE-")) {
        return clean
    }
    return "CEE-${clean.replace(" ", "-")}"
}

fun mergeCameraEventEstimateLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
