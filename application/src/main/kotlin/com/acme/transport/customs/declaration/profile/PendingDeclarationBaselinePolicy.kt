package com.acme.transport.customs.declaration.profile

import com.acme.transport.kit.numbers.roundTo

/** Puts a pending declaration baseline code in its canonical PDB- form. */
fun normalizePendingDeclarationBaselineCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PDB-")) {
        return clean
    }
    return "PDB-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 81395.0, in order. */
fun pendingDeclarationBaselineBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 81395.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun pendingDeclarationBaselineBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
