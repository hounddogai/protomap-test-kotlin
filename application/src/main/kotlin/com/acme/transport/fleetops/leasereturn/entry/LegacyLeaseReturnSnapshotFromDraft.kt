package com.acme.transport.fleetops.leasereturn.entry

import com.acme.transport.fleetops.leasereturn.template.LeaseReturnDraft
import com.acme.transport.fleetops.leasereturn.template.leaseReturnTemplateWeight

fun summarizeLeaseReturnDraftsForSnapshot(items: MutableList<LeaseReturnDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveLegacyLeaseReturnSnapshotsFromDraft(items: MutableList<LeaseReturnDraft>): MutableList<LegacyLeaseReturnSnapshot> {
    val out = mutableListOf<LegacyLeaseReturnSnapshot>()
    for (item in items) {
        val derived = newLegacyLeaseReturnSnapshot(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun planLegacyLeaseReturnSnapshot(item: LegacyLeaseReturnSnapshot): Double = leaseReturnTemplateWeight(3, true)
