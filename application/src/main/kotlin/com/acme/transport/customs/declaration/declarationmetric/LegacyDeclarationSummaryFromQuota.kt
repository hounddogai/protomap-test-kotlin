package com.acme.transport.customs.declaration.declarationmetric

import com.acme.transport.customs.declaration.declarationbatch.RegionalDeclarationQuota
import com.acme.transport.customs.declaration.declarationbatch.normalizeRegionalDeclarationQuotaCode

fun summarizeRegionalDeclarationQuotasForSummary(items: MutableList<RegionalDeclarationQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveLegacyDeclarationSummariesFromQuota(items: MutableList<RegionalDeclarationQuota>): MutableList<LegacyDeclarationSummary> {
    val out = mutableListOf<LegacyDeclarationSummary>()
    for (item in items) {
        val derived = newLegacyDeclarationSummary(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeLegacyDeclarationSummary(item: LegacyDeclarationSummary): String {
    return normalizeRegionalDeclarationQuotaCode(item.code)
}
