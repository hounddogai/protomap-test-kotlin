package com.acme.transport.customs.tariff.profile

import com.acme.transport.customs.declaration.review.LenientDeclarationCheckpoint
import com.acme.transport.customs.declaration.review.classifyLenientDeclarationCheckpoint

fun summarizeLenientDeclarationCheckpointsForAllocation(items: MutableList<LenientDeclarationCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveTariffAllocationsFromCheckpoint(items: MutableList<LenientDeclarationCheckpoint>): MutableList<TariffAllocation> {
    val out = mutableListOf<TariffAllocation>()
    for (item in items) {
        val derived = newTariffAllocation(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun applyTariffAllocation(item: TariffAllocation): String {
    return classifyLenientDeclarationCheckpoint(item.weightedLoad())
}
