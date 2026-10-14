package com.acme.transport.fleetops.defect.allocation

import com.acme.transport.fleetops.defect.claim.DefectProfile
import com.acme.transport.fleetops.defect.claim.defectClaimBucket

fun summarizeDefectProfilesForSlot(items: MutableList<DefectProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveDefectSlotsFromProfile(items: MutableList<DefectProfile>): MutableList<DefectSlot> {
    val out = mutableListOf<DefectSlot>()
    for (item in items) {
        val derived = newDefectSlot(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreDefectSlot(item: DefectSlot): String = defectClaimBucket(item.measuredValue())
