package com.acme.transport.customs.clearance.seasonalclearanceoverride

import com.acme.transport.customs.tariff.batch.TariffNotice
import com.acme.transport.customs.tariff.batch.normalizeStrictTariffBatchCode

fun summarizeTariffNoticesForOverride(items: MutableList<TariffNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveGlobalClearanceOverridesFromNotice(items: MutableList<TariffNotice>): MutableList<GlobalClearanceOverride> {
    val out = mutableListOf<GlobalClearanceOverride>()
    for (item in items) {
        val derived = newGlobalClearanceOverride(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun deriveGlobalClearanceOverride(item: GlobalClearanceOverride): String {
    return normalizeStrictTariffBatchCode(item.code)
}
