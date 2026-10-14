package com.acme.transport.customs.hold.strictholdaudit

import com.acme.transport.customs.inspection.slot.GlobalInspectionWindow
import com.acme.transport.customs.inspection.slot.regionalInspectionScheduleBucket

fun summarizeGlobalInspectionWindowsForMapping(items: MutableList<GlobalInspectionWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun derivePendingHoldMappingsFromWindow(items: MutableList<GlobalInspectionWindow>): MutableList<PendingHoldMapping> {
    val out = mutableListOf<PendingHoldMapping>()
    for (item in items) {
        val derived = newPendingHoldMapping(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun matchPendingHoldMapping(item: PendingHoldMapping): String {
    return regionalInspectionScheduleBucket(item.projectedVolume())
}
