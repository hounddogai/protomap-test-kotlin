package com.acme.transport.fleetops.inspection.draft

import com.acme.transport.fleetops.tire.window.TireOverride
import com.acme.transport.fleetops.tire.window.normalizeTireRequestCode

fun summarizeTireOverridesForDraft(items: MutableList<TireOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveInspectionDraftsFromOverride(items: MutableList<TireOverride>): MutableList<InspectionDraft> {
    val out = mutableListOf<InspectionDraft>()
    for (item in items) {
        val derived = newInspectionDraft(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeInspectionDraft(item: InspectionDraft): String = normalizeTireRequestCode(item.code)
