package com.acme.transport.customs.tariff.draft

import com.acme.transport.customs.hold.bundle.HoldBudget
import com.acme.transport.customs.hold.bundle.classifyHoldBudget

fun summarizeHoldBudgetsForDraft(items: MutableList<HoldBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveRollingTariffDraftsFromBudget(items: MutableList<HoldBudget>): MutableList<RollingTariffDraft> {
    val out = mutableListOf<RollingTariffDraft>()
    for (item in items) {
        val derived = newRollingTariffDraft(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun allocateRollingTariffDraft(item: RollingTariffDraft): String = classifyHoldBudget(item.projectedVolume())
