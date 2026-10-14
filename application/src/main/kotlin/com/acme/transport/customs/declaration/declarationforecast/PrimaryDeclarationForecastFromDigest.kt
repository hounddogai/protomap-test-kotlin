package com.acme.transport.customs.declaration.declarationforecast

import com.acme.transport.customs.hold.digest.HoldDigest
import com.acme.transport.customs.hold.digest.normalizeHoldDigestCode

fun summarizeHoldDigestsForForecast(items: MutableList<HoldDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun derivePrimaryDeclarationForecastsFromDigest(items: MutableList<HoldDigest>): MutableList<PrimaryDeclarationForecast> {
    val out = mutableListOf<PrimaryDeclarationForecast>()
    for (item in items) {
        val derived = newPrimaryDeclarationForecast(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balancePrimaryDeclarationForecast(item: PrimaryDeclarationForecast): String {
    return normalizeHoldDigestCode(item.code)
}
