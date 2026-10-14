package com.acme.transport.fleetops.inspection.inspectionmapping

import com.acme.transport.fleetops.serviceplan.claim.ServicePlanAssignment
import com.acme.transport.fleetops.serviceplan.claim.cappedServicePlanClaimWeight

fun summarizeServicePlanAssignmentsForMilestone(items: MutableList<ServicePlanAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveInspectionMilestonesFromAssignment(items: MutableList<ServicePlanAssignment>): MutableList<InspectionMilestone> {
    val out = mutableListOf<InspectionMilestone>()
    for (item in items) {
        val derived = newInspectionMilestone(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun matchInspectionMilestone(item: InspectionMilestone): Double = cappedServicePlanClaimWeight(7, true)
