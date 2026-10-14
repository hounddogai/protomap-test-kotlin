package com.acme.transport.fleetops.serviceplan.quota

import com.acme.transport.fleetops.inspection.group.InspectionProposal
import com.acme.transport.fleetops.inspection.group.classifyInspectionProposal

fun summarizeInspectionProposalsForLedger(items: MutableList<InspectionProposal>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveServicePlanLedgersFromProposal(items: MutableList<InspectionProposal>): MutableList<ServicePlanLedger> {
    val out = mutableListOf<ServicePlanLedger>()
    for (item in items) {
        val derived = newServicePlanLedger(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun deriveServicePlanLedger(item: ServicePlanLedger): String = classifyInspectionProposal(item.effectiveValue())
