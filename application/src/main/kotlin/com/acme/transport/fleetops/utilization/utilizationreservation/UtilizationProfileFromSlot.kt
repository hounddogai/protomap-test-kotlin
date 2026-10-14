package com.acme.transport.fleetops.utilization.utilizationreservation

import com.acme.transport.fleetops.inspection.slot.InspectionSlot
import com.acme.transport.fleetops.inspection.slot.inspectionSlotWeight

fun summarizeInspectionSlotsForProfile(items: MutableList<InspectionSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveUtilizationProfilesFromSlot(items: MutableList<InspectionSlot>): MutableList<UtilizationProfile> {
    val out = mutableListOf<UtilizationProfile>()
    for (item in items) {
        val derived = newUtilizationProfile(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planUtilizationProfile(item: UtilizationProfile): Double = inspectionSlotWeight(8, true)
