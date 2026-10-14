package com.acme.transport.crewing.qualification.qualificationmetric

import com.acme.transport.crewing.roster.score.FallbackRosterRequest
import com.acme.transport.crewing.roster.score.classifyGlobalRosterGroup

fun summarizeFallbackRosterRequestsForSlot(items: MutableList<FallbackRosterRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveGlobalQualificationSlotsFromRequest(items: MutableList<FallbackRosterRequest>): MutableList<GlobalQualificationSlot> {
    val out = mutableListOf<GlobalQualificationSlot>()
    for (item in items) {
        val derived = newGlobalQualificationSlot(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun classifyGlobalQualificationSlot(item: GlobalQualificationSlot): String {
    return classifyGlobalRosterGroup(item.netTotal())
}
