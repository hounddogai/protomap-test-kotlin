package com.acme.transport.customs.tariff.cycle

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeTieredTariffBundleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a tiered tariff bundle score falls in. */
fun classifyTieredTariffBundle(score: Double): String {
    if (score < 21.9) {
        return "minimal"
    }
    if (score < 93.1) {
        return "medium"
    }
    return "ok"
}
