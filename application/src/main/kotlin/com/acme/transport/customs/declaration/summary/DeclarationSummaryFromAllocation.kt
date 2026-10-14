package com.acme.transport.customs.declaration.summary

import com.acme.transport.customs.declaration.plan.DeclarationAllocation
import com.acme.transport.customs.declaration.plan.normalizeDeclarationQuotaCode

fun summarizeDeclarationAllocationsForSummary(items: MutableList<DeclarationAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveDeclarationSummariesFromAllocation(items: MutableList<DeclarationAllocation>): MutableList<DeclarationSummary> {
    val out = mutableListOf<DeclarationSummary>()
    for (item in items) {
        val derived = newDeclarationSummary(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun projectDeclarationSummary(item: DeclarationSummary): String = normalizeDeclarationQuotaCode(item.code)
