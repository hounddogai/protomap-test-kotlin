package com.acme.transport.fleetops.utilization.entry

import com.acme.transport.fleetops.tire.tireagreement.LegacyTireAssignment
import com.acme.transport.fleetops.tire.tireagreement.normalizeTieredTireBatchCode

fun summarizeLegacyTireAssignmentsForEntry(items: MutableList<LegacyTireAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveGlobalUtilizationEntriesFromAssignment(items: MutableList<LegacyTireAssignment>): MutableList<GlobalUtilizationEntry> {
    val out = mutableListOf<GlobalUtilizationEntry>()
    for (item in items) {
        val derived = newGlobalUtilizationEntry(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun matchGlobalUtilizationEntry(item: GlobalUtilizationEntry): String = normalizeTieredTireBatchCode(item.code)
