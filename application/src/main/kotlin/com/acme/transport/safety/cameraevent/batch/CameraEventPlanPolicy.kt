package com.acme.transport.safety.cameraevent.batch

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a camera event plan code in its canonical CEP- form. */
fun normalizeCameraEventPlanCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CEP-")) {
        return clean
    }
    return "CEP-${clean.replace(" ", "-")}"
}

fun mergeCameraEventPlanLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
