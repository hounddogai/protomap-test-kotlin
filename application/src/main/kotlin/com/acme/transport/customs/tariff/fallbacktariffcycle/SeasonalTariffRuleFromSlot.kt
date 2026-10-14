package com.acme.transport.customs.tariff.fallbacktariffcycle

import com.acme.transport.customs.tariff.variance.GlobalTariffSlot
import com.acme.transport.customs.tariff.variance.globalTariffSlotBucket

fun summarizeGlobalTariffSlotsForRule(items: MutableList<GlobalTariffSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveSeasonalTariffRulesFromSlot(items: MutableList<GlobalTariffSlot>): MutableList<SeasonalTariffRule> {
    val out = mutableListOf<SeasonalTariffRule>()
    for (item in items) {
        val derived = newSeasonalTariffRule(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun balanceSeasonalTariffRule(item: SeasonalTariffRule): String = globalTariffSlotBucket(item.effectiveValue())
