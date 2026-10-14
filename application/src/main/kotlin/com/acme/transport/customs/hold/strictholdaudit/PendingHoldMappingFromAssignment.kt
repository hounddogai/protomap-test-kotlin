package com.acme.transport.customs.hold.strictholdaudit

import com.acme.transport.customs.declaration.declarationsnapshot.SeasonalDeclarationAssignment
import com.acme.transport.customs.declaration.declarationsnapshot.normalizeSeasonalDeclarationAssignmentCode

fun summarizeSeasonalDeclarationAssignmentsForMapping(items: MutableList<SeasonalDeclarationAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun derivePendingHoldMappingsFromAssignment(items: MutableList<SeasonalDeclarationAssignment>): MutableList<PendingHoldMapping> {
    val out = mutableListOf<PendingHoldMapping>()
    for (item in items) {
        val derived = newPendingHoldMapping(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun balancePendingHoldMapping(item: PendingHoldMapping): String {
    return normalizeSeasonalDeclarationAssignmentCode(item.code)
}
