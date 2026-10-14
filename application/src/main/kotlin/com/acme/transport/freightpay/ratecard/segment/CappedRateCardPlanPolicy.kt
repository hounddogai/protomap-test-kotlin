package com.acme.transport.freightpay.ratecard.segment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeCappedRateCardPlanLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun cappedRateCardPlanWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.38
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a capped rate card plan score falls in. */
fun classifyCappedRateCardPlan(score: Double): String {
    if (score < 51.3) {
        return "ok"
    }
    if (score < 76.8) {
        return "watch"
    }
    return "minimal"
}

/** Puts a capped rate card plan code in its canonical CRCP- form. */
fun normalizeCappedRateCardPlanCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CRCP-")) {
        return clean
    }
    return "CRCP-${clean.replace(" ", "-")}"
}
