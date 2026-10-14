package com.acme.transport.customs.declaration.plan

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a declaration quota code in its canonical DQ- form. */
fun normalizeDeclarationQuotaCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DQ-")) {
        return clean
    }
    return "DQ-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 68104.0, in order. */
fun declarationQuotaBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 68104.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeDeclarationQuotaLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
