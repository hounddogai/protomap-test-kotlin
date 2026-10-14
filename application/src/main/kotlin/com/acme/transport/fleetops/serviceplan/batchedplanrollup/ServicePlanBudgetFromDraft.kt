package com.acme.transport.fleetops.serviceplan.batchedplanrollup

import com.acme.transport.fleetops.leasereturn.template.LeaseReturnDraft
import com.acme.transport.fleetops.leasereturn.template.normalizeLeaseReturnCheckpointCode

fun summarizeLeaseReturnDraftsForBudget(items: MutableList<LeaseReturnDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveServicePlanBudgetsFromDraft(items: MutableList<LeaseReturnDraft>): MutableList<ServicePlanBudget> {
    val out = mutableListOf<ServicePlanBudget>()
    for (item in items) {
        val derived = newServicePlanBudget(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun mergeServicePlanBudget(item: ServicePlanBudget): String = normalizeLeaseReturnCheckpointCode(item.code)
