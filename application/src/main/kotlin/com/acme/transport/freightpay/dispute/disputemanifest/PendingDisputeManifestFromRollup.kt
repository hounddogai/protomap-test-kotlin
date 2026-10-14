package com.acme.transport.freightpay.dispute.disputemanifest

import com.acme.transport.freightpay.accessorial.audit.DefaultAccessorialRollup
import com.acme.transport.freightpay.accessorial.audit.normalizeCappedAccessorialAuditCode

fun summarizeDefaultAccessorialRollupsForManifest(items: MutableList<DefaultAccessorialRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun derivePendingDisputeManifestsFromRollup(items: MutableList<DefaultAccessorialRollup>): MutableList<PendingDisputeManifest> {
    val out = mutableListOf<PendingDisputeManifest>()
    for (item in items) {
        val derived = newPendingDisputeManifest(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectPendingDisputeManifest(item: PendingDisputeManifest): String {
    return normalizeCappedAccessorialAuditCode(item.code)
}
