package com.acme.transport.fleetops.leasereturn.returnpreference

import com.acme.transport.fleetops.serviceplan.entry.ServicePlanBatch
import com.acme.transport.fleetops.serviceplan.entry.normalizeServicePlanBatchCode

fun summarizeServicePlanBatchesForProposal(items: MutableList<ServicePlanBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveAdaptiveLeaseReturnProposalsFromBatch(items: MutableList<ServicePlanBatch>): MutableList<AdaptiveLeaseReturnProposal> {
    val out = mutableListOf<AdaptiveLeaseReturnProposal>()
    for (item in items) {
        val derived = newAdaptiveLeaseReturnProposal(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveAdaptiveLeaseReturnProposal(item: AdaptiveLeaseReturnProposal): String {
    return normalizeServicePlanBatchCode(item.code)
}
