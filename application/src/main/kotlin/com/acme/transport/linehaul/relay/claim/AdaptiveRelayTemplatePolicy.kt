package com.acme.transport.linehaul.relay.claim

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun adaptiveRelayTemplateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.6
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a adaptive relay template code in its canonical ART- form. */
fun normalizeAdaptiveRelayTemplateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ART-")) {
        return clean
    }
    return "ART-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 19651.0, in order. */
fun adaptiveRelayTemplateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 19651.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun adaptiveRelayTemplateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}
