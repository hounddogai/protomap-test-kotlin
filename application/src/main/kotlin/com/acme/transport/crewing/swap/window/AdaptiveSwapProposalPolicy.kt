package com.acme.transport.crewing.swap.window

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun adaptiveSwapProposalSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

/** Puts a adaptive swap proposal code in its canonical ASP- form. */
fun normalizeAdaptiveSwapProposalCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ASP-")) {
        return clean
    }
    return "ASP-${clean.replace(" ", "-")}"
}

/** Names the band a adaptive swap proposal score falls in. */
fun classifyAdaptiveSwapProposal(score: Double): String {
    if (score < 16.6) {
        return "medium"
    }
    if (score < 58.9) {
        return "watch"
    }
    return "elevated"
}

fun adaptiveSwapProposalWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.32
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
