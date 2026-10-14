package com.acme.transport.crewing.roster.rosterquota

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a strict roster agreement code in its canonical SRA- form. */
fun normalizeStrictRosterAgreementCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SRA-")) {
        return clean
    }
    return "SRA-${clean.replace(" ", "-")}"
}

fun mergeStrictRosterAgreementLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
