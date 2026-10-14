package com.acme.transport.safety.nearmiss.audit

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergePrimaryNearMissPolicyLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a primary near miss policy code in its canonical PNMP- form. */
fun normalizePrimaryNearMissPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PNMP-")) {
        return clean
    }
    return "PNMP-${clean.replace(" ", "-")}"
}
