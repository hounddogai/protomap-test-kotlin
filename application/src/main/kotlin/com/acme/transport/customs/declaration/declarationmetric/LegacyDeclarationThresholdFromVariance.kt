package com.acme.transport.customs.declaration.declarationmetric

import com.acme.transport.customs.broker.exception.RegionalBrokerVariance
import com.acme.transport.customs.broker.exception.normalizeBrokerExceptionCode

fun summarizeRegionalBrokerVariancesForThreshold(items: MutableList<RegionalBrokerVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveLegacyDeclarationThresholdsFromVariance(items: MutableList<RegionalBrokerVariance>): MutableList<LegacyDeclarationThreshold> {
    val out = mutableListOf<LegacyDeclarationThreshold>()
    for (item in items) {
        val derived = newLegacyDeclarationThreshold(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun deriveLegacyDeclarationThreshold(item: LegacyDeclarationThreshold): String {
    return normalizeBrokerExceptionCode(item.code)
}
