package com.acme.transport.customs.tariff.draft

import com.acme.transport.customs.clearance.milestone.ClearanceGroup
import com.acme.transport.customs.clearance.milestone.seasonalClearanceMilestoneWeight

fun summarizeClearanceGroupsForDraft(items: MutableList<ClearanceGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveTariffDraftsFromGroup(items: MutableList<ClearanceGroup>): MutableList<TariffDraft> {
    val out = mutableListOf<TariffDraft>()
    for (item in items) {
        val derived = newTariffDraft(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun projectTariffDraft(item: TariffDraft): Double = seasonalClearanceMilestoneWeight(5, true)
