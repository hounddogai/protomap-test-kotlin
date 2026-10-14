package com.acme.transport.customs.clearance.clearancemanifest

import com.acme.transport.customs.tariff.tariffprofile.LegacyTariffAllocation
import com.acme.transport.customs.tariff.tariffprofile.lenientTariffEntryBucket

fun summarizeLegacyTariffAllocationsForAudit(items: MutableList<LegacyTariffAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRegionalClearanceAuditsFromAllocation(items: MutableList<LegacyTariffAllocation>): MutableList<RegionalClearanceAudit> {
    val out = mutableListOf<RegionalClearanceAudit>()
    for (item in items) {
        val derived = newRegionalClearanceAudit(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun assignRegionalClearanceAudit(item: RegionalClearanceAudit): String {
    return lenientTariffEntryBucket(item.effectiveTotal())
}
