package com.acme.transport.customs.declaration.review

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Puts a declaration review code in its canonical DR- form. */
fun normalizeDeclarationReviewCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DR-")) {
        return clean
    }
    return "DR-${clean.replace(" ", "-")}"
}

fun mergeDeclarationReviewLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun declarationReviewWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.43
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
