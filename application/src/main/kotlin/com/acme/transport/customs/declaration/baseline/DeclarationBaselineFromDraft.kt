package com.acme.transport.customs.declaration.baseline

import com.acme.transport.customs.tariff.draft.RollingTariffDraft
import com.acme.transport.customs.tariff.draft.classifyRollingTariffDraft

fun summarizeRollingTariffDraftsForBaseline(items: MutableList<RollingTariffDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveDeclarationBaselinesFromDraft(items: MutableList<RollingTariffDraft>): MutableList<DeclarationBaseline> {
    val out = mutableListOf<DeclarationBaseline>()
    for (item in items) {
        val derived = newDeclarationBaseline(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignDeclarationBaseline(item: DeclarationBaseline): String = classifyRollingTariffDraft(item.adjustedLoad())
