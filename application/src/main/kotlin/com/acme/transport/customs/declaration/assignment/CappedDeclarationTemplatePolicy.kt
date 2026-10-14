package com.acme.transport.customs.declaration.assignment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun cappedDeclarationTemplateBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Puts a capped declaration template code in its canonical CDT- form. */
fun normalizeCappedDeclarationTemplateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CDT-")) {
        return clean
    }
    return "CDT-${clean.replace(" ", "-")}"
}

/** Names the band a capped declaration template score falls in. */
fun classifyCappedDeclarationTemplate(score: Double): String {
    if (score < 3.9) {
        return "watch"
    }
    if (score < 25.2) {
        return "high"
    }
    if (score < 25.2) {
        return "minimal"
    }
    if (score < 36.4) {
        return "low"
    }
    return "elevated"
}

fun mergeCappedDeclarationTemplateLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
