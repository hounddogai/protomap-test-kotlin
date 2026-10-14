package com.acme.transport.customs.declaration.declarationsnapshot

import com.acme.transport.customs.tariff.tariffprofile.LenientTariffEntry
import com.acme.transport.customs.tariff.tariffprofile.lenientTariffEntryBucket

fun summarizeLenientTariffEntriesForAssignment(items: MutableList<LenientTariffEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveSeasonalDeclarationAssignmentsFromEntry(items: MutableList<LenientTariffEntry>): MutableList<SeasonalDeclarationAssignment> {
    val out = mutableListOf<SeasonalDeclarationAssignment>()
    for (item in items) {
        val derived = newSeasonalDeclarationAssignment(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceSeasonalDeclarationAssignment(item: SeasonalDeclarationAssignment): String {
    return lenientTariffEntryBucket(item.weightedTotal())
}
