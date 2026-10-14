package com.acme.transport.crewing.qualification.qualificationproposal

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergePrimaryQualificationReviewLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a primary qualification review code in its canonical PQR- form. */
fun normalizePrimaryQualificationReviewCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PQR-")) {
        return clean
    }
    return "PQR-${clean.replace(" ", "-")}"
}
