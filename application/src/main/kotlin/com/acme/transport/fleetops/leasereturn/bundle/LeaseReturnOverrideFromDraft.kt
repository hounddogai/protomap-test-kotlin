package com.acme.transport.fleetops.leasereturn.bundle

import com.acme.transport.fleetops.inspection.draft.InspectionDraft
import com.acme.transport.fleetops.inspection.draft.normalizeInspectionDraftCode

fun summarizeInspectionDraftsForOverride(items: MutableList<InspectionDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveLeaseReturnOverridesFromDraft(items: MutableList<InspectionDraft>): MutableList<LeaseReturnOverride> {
    val out = mutableListOf<LeaseReturnOverride>()
    for (item in items) {
        val derived = newLeaseReturnOverride(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun allocateLeaseReturnOverride(item: LeaseReturnOverride): String = normalizeInspectionDraftCode(item.code)
