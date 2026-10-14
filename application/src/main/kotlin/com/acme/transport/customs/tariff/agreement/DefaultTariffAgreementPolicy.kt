package com.acme.transport.customs.tariff.agreement

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun defaultTariffAgreementSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

fun mergeDefaultTariffAgreementLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun defaultTariffAgreementWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.78
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a default tariff agreement score falls in. */
fun classifyDefaultTariffAgreement(score: Double): String {
    if (score < 8.0) {
        return "medium"
    }
    if (score < 27.9) {
        return "ok"
    }
    if (score < 29.3) {
        return "minimal"
    }
    return "critical"
}
