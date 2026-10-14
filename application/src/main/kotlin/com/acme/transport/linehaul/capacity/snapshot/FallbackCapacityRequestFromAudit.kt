package com.acme.transport.linehaul.capacity.snapshot

import com.acme.transport.linehaul.backhaul.ledger.ScheduledBackhaulAudit
import com.acme.transport.linehaul.backhaul.ledger.globalBackhaulLedgerBucket

fun summarizeScheduledBackhaulAuditsForRequest(items: MutableList<ScheduledBackhaulAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveFallbackCapacityRequestsFromAudit(items: MutableList<ScheduledBackhaulAudit>): MutableList<FallbackCapacityRequest> {
    val out = mutableListOf<FallbackCapacityRequest>()
    for (item in items) {
        val derived = newFallbackCapacityRequest(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreFallbackCapacityRequest(item: FallbackCapacityRequest): String {
    return globalBackhaulLedgerBucket(item.projectedExposure())
}
