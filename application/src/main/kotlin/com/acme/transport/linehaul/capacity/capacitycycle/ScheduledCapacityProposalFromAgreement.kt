package com.acme.transport.linehaul.capacity.capacitycycle

import com.acme.transport.linehaul.capacity.target.BatchedCapacityAgreement
import com.acme.transport.linehaul.capacity.target.pendingCapacityEntryWeight

fun summarizeBatchedCapacityAgreementsForProposal(items: MutableList<BatchedCapacityAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveScheduledCapacityProposalsFromAgreement(items: MutableList<BatchedCapacityAgreement>): MutableList<ScheduledCapacityProposal> {
    val out = mutableListOf<ScheduledCapacityProposal>()
    for (item in items) {
        val derived = newScheduledCapacityProposal(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun assignScheduledCapacityProposal(item: ScheduledCapacityProposal): Double = pendingCapacityEntryWeight(4, true)
