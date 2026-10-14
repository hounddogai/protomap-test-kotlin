package com.acme.transport.freightpay.audit.policy

import com.acme.transport.freightpay.dispute.digest.DefaultDisputeSnapshot
import com.acme.transport.freightpay.dispute.digest.normalizeDefaultDisputeSnapshotCode

fun summarizeDefaultDisputeSnapshotsForPolicy(items: MutableList<DefaultDisputeSnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveAuditPoliciesFromSnapshot(items: MutableList<DefaultDisputeSnapshot>): MutableList<AuditPolicy> {
    val out = mutableListOf<AuditPolicy>()
    for (item in items) {
        val derived = newAuditPolicy(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeAuditPolicy(item: AuditPolicy): String = normalizeDefaultDisputeSnapshotCode(item.code)
