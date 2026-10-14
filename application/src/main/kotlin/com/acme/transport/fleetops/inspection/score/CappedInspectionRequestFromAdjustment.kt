package com.acme.transport.fleetops.inspection.score

import com.acme.transport.fleetops.tire.tiremanifest.TieredTireAdjustment
import com.acme.transport.fleetops.tire.tiremanifest.normalizeLegacyTireRequestCode

fun summarizeTieredTireAdjustmentsForRequest(items: MutableList<TieredTireAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveCappedInspectionRequestsFromAdjustment(items: MutableList<TieredTireAdjustment>): MutableList<CappedInspectionRequest> {
    val out = mutableListOf<CappedInspectionRequest>()
    for (item in items) {
        val derived = newCappedInspectionRequest(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeCappedInspectionRequest(item: CappedInspectionRequest): String {
    return normalizeLegacyTireRequestCode(item.code)
}
