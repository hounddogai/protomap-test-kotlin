package com.acme.transport.customs.declaration.assignment

import com.acme.transport.kit.numbers.roundTo

fun declarationPolicyBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Puts a declaration policy code in its canonical DP- form. */
fun normalizeDeclarationPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DP-")) {
        return clean
    }
    return "DP-${clean.replace(" ", "-")}"
}

/** Names the band a declaration policy score falls in. */
fun classifyDeclarationPolicy(score: Double): String {
    if (score < 5.6) {
        return "elevated"
    }
    if (score < 12.6) {
        return "high"
    }
    if (score < 18.8) {
        return "ok"
    }
    if (score < 88.2) {
        return "medium"
    }
    return "minimal"
}

/** Counts how many amounts fit in a budget of 35716.0, in order. */
fun declarationPolicyBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 35716.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
