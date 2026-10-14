package com.acme.transport.customs.declaration.digest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a fallback declaration target code in its canonical FDT- form. */
fun normalizeFallbackDeclarationTargetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FDT-")) {
        return clean
    }
    return "FDT-${clean.replace(" ", "-")}"
}

fun mergeFallbackDeclarationTargetLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 88294.0, in order. */
fun fallbackDeclarationTargetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 88294.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
