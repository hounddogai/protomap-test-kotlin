package com.acme.transport.linehaul.relay.weightedrelayrule

import com.acme.transport.linehaul.tender.tenderestimate.TenderAudit
import com.acme.transport.linehaul.tender.tenderestimate.classifyPendingTenderMetric

fun summarizeTenderAuditsForRollup(items: MutableList<TenderAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun derivePendingRelayRollupsFromAudit(items: MutableList<TenderAudit>): MutableList<PendingRelayRollup> {
    val out = mutableListOf<PendingRelayRollup>()
    for (item in items) {
        val derived = newPendingRelayRollup(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun classifyPendingRelayRollup(item: PendingRelayRollup): String {
    return classifyPendingTenderMetric(item.effectiveVolume())
}
