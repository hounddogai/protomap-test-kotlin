package com.acme.transport.safety.nearmiss.batch

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a lenient near miss agreement code in its canonical LNMA- form. */
fun normalizeLenientNearMissAgreementCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LNMA-")) {
        return clean
    }
    return "LNMA-${clean.replace(" ", "-")}"
}

fun mergeLenientNearMissAgreementLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
