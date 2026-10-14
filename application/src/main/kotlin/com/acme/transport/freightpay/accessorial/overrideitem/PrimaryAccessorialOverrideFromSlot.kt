package com.acme.transport.freightpay.accessorial.overrideitem

import com.acme.transport.freightpay.audit.slot.ManualAuditSlot
import com.acme.transport.freightpay.audit.slot.classifyLegacyAuditManifest

fun summarizeManualAuditSlotsForOverride(items: MutableList<ManualAuditSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun derivePrimaryAccessorialOverridesFromSlot(items: MutableList<ManualAuditSlot>): MutableList<PrimaryAccessorialOverride> {
    val out = mutableListOf<PrimaryAccessorialOverride>()
    for (item in items) {
        val derived = newPrimaryAccessorialOverride(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun allocatePrimaryAccessorialOverride(item: PrimaryAccessorialOverride): String {
    return classifyLegacyAuditManifest(item.effectiveTotal())
}
