package com.acme.transport.emissions.emissionfactor.digest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeEmissionFactorTemplateLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a emission factor template code in its canonical EFT- form. */
fun normalizeEmissionFactorTemplateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("EFT-")) {
        return clean
    }
    return "EFT-${clean.replace(" ", "-")}"
}
