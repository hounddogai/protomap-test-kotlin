package com.acme.transport.linehaul.tender.tier

import com.acme.transport.linehaul.capacity.capacitysnapshot.PrimaryCapacitySnapshot
import com.acme.transport.linehaul.capacity.capacitysnapshot.classifyPrimaryCapacitySnapshot

fun summarizePrimaryCapacitySnapshotsForRollup(items: MutableList<PrimaryCapacitySnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveManualTenderRollupsFromSnapshot(items: MutableList<PrimaryCapacitySnapshot>): MutableList<ManualTenderRollup> {
    val out = mutableListOf<ManualTenderRollup>()
    for (item in items) {
        val derived = newManualTenderRollup(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleManualTenderRollup(item: ManualTenderRollup): String {
    return classifyPrimaryCapacitySnapshot(item.projectedValue())
}
