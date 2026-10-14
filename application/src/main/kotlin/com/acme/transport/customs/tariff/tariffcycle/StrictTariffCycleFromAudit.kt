package com.acme.transport.customs.tariff.tariffcycle

import com.acme.transport.customs.hold.variance.SeasonalHoldAudit
import com.acme.transport.customs.hold.variance.batchedHoldVarianceBucket

fun summarizeSeasonalHoldAuditsForCycle(items: MutableList<SeasonalHoldAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveStrictTariffCyclesFromAudit(items: MutableList<SeasonalHoldAudit>): MutableList<StrictTariffCycle> {
    val out = mutableListOf<StrictTariffCycle>()
    for (item in items) {
        val derived = newStrictTariffCycle(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun projectStrictTariffCycle(item: StrictTariffCycle): String = batchedHoldVarianceBucket(item.measuredValue())
