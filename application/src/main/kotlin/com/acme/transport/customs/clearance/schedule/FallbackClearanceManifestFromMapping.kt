package com.acme.transport.customs.clearance.schedule

import com.acme.transport.customs.hold.strictholdaudit.PendingHoldMapping
import com.acme.transport.customs.hold.strictholdaudit.normalizeStrictHoldAuditCode

fun summarizePendingHoldMappingsForManifest(items: MutableList<PendingHoldMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveFallbackClearanceManifestsFromMapping(items: MutableList<PendingHoldMapping>): MutableList<FallbackClearanceManifest> {
    val out = mutableListOf<FallbackClearanceManifest>()
    for (item in items) {
        val derived = newFallbackClearanceManifest(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleFallbackClearanceManifest(item: FallbackClearanceManifest): String {
    return normalizeStrictHoldAuditCode(item.code)
}
