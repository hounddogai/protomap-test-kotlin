package com.acme.transport.crewing.availability.availabilityscore

import com.acme.transport.crewing.roster.regionalrostersummary.WeightedRosterAssignment
import com.acme.transport.crewing.roster.regionalrostersummary.classifyTieredRosterPlan

fun summarizeWeightedRosterAssignmentsForPlan(items: MutableList<WeightedRosterAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun derivePrimaryAvailabilityPlansFromAssignment(items: MutableList<WeightedRosterAssignment>): MutableList<PrimaryAvailabilityPlan> {
    val out = mutableListOf<PrimaryAvailabilityPlan>()
    for (item in items) {
        val derived = newPrimaryAvailabilityPlan(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun allocatePrimaryAvailabilityPlan(item: PrimaryAvailabilityPlan): String {
    return classifyTieredRosterPlan(item.netLoad())
}
