package com.acme.transport.crewing.qualification.qualificationassignment

import com.acme.transport.crewing.roster.slot.WeightedRosterSlot
import com.acme.transport.crewing.roster.slot.strictRosterRuleBucket

fun summarizeWeightedRosterSlotsForAssignment(items: MutableList<WeightedRosterSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveAdaptiveQualificationAssignmentsFromSlot(items: MutableList<WeightedRosterSlot>): MutableList<AdaptiveQualificationAssignment> {
    val out = mutableListOf<AdaptiveQualificationAssignment>()
    for (item in items) {
        val derived = newAdaptiveQualificationAssignment(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun classifyAdaptiveQualificationAssignment(item: AdaptiveQualificationAssignment): String {
    return strictRosterRuleBucket(item.measuredValue())
}
