package com.acme.transport.customs.declaration.rule

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a fallback declaration record score falls in. */
fun classifyFallbackDeclarationRecord(score: Double): String {
    if (score < 19.3) {
        return "ok"
    }
    if (score < 24.0) {
        return "watch"
    }
    if (score < 88.6) {
        return "high"
    }
    return "minimal"
}

/** Counts how many amounts fit in a budget of 79691.0, in order. */
fun fallbackDeclarationRecordBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 79691.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a fallback declaration record code in its canonical FDR- form. */
fun normalizeFallbackDeclarationRecordCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FDR-")) {
        return clean
    }
    return "FDR-${clean.replace(" ", "-")}"
}

fun fallbackDeclarationRecordWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.46
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
