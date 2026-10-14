package com.acme.transport.customs.declaration.manifest

import com.acme.transport.kit.numbers.roundTo

fun declarationManifestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

fun declarationManifestBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Puts a declaration manifest code in its canonical DM- form. */
fun normalizeDeclarationManifestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DM-")) {
        return clean
    }
    return "DM-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 8809.0, in order. */
fun declarationManifestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 8809.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
