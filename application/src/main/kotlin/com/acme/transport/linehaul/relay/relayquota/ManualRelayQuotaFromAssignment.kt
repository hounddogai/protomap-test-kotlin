package com.acme.transport.linehaul.relay.relayquota

import com.acme.transport.linehaul.tender.assignment.TenderAssignment
import com.acme.transport.linehaul.tender.assignment.classifyTenderAssignment

fun summarizeTenderAssignmentsForQuota(items: MutableList<TenderAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveManualRelayQuotasFromAssignment(items: MutableList<TenderAssignment>): MutableList<ManualRelayQuota> {
    val out = mutableListOf<ManualRelayQuota>()
    for (item in items) {
        val derived = newManualRelayQuota(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectManualRelayQuota(item: ManualRelayQuota): String = classifyTenderAssignment(item.weightedVolume())
