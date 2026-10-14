package com.acme.transport.customs.broker.window

import com.acme.transport.customs.declaration.declarationsnapshot.SeasonalDeclarationAssignment
import com.acme.transport.customs.declaration.declarationsnapshot.normalizeSeasonalDeclarationAssignmentCode

fun summarizeSeasonalDeclarationAssignmentsForSchedule(items: MutableList<SeasonalDeclarationAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveTieredBrokerSchedulesFromAssignment(items: MutableList<SeasonalDeclarationAssignment>): MutableList<TieredBrokerSchedule> {
    val out = mutableListOf<TieredBrokerSchedule>()
    for (item in items) {
        val derived = newTieredBrokerSchedule(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeTieredBrokerSchedule(item: TieredBrokerSchedule): String {
    return normalizeSeasonalDeclarationAssignmentCode(item.code)
}
