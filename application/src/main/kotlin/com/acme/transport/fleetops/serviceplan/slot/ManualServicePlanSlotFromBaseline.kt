package com.acme.transport.fleetops.serviceplan.slot

import com.acme.transport.fleetops.tire.slot.TireBaseline
import com.acme.transport.fleetops.tire.slot.normalizeBatchedTireManifestCode

fun summarizeTireBaselinesForSlot(items: MutableList<TireBaseline>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveManualServicePlanSlotsFromBaseline(items: MutableList<TireBaseline>): MutableList<ManualServicePlanSlot> {
    val out = mutableListOf<ManualServicePlanSlot>()
    for (item in items) {
        val derived = newManualServicePlanSlot(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun projectManualServicePlanSlot(item: ManualServicePlanSlot): String {
    return normalizeBatchedTireManifestCode(item.code)
}
