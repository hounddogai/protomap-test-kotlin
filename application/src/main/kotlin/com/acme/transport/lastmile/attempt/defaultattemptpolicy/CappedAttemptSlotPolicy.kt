package com.acme.transport.lastmile.attempt.defaultattemptpolicy

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a capped attempt slot code in its canonical CAS- form. */
fun normalizeCappedAttemptSlotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CAS-")) {
        return clean
    }
    return "CAS-${clean.replace(" ", "-")}"
}

fun mergeCappedAttemptSlotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
