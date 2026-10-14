package com.acme.transport.fleetops.defect.cycle

import com.acme.transport.fleetops.inspection.score.PrimaryInspectionException
import com.acme.transport.fleetops.inspection.score.cappedInspectionRequestBucket

fun summarizePrimaryInspectionExceptionsForAllocation(items: MutableList<PrimaryInspectionException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRegionalDefectAllocationsFromException(items: MutableList<PrimaryInspectionException>): MutableList<RegionalDefectAllocation> {
    val out = mutableListOf<RegionalDefectAllocation>()
    for (item in items) {
        val derived = newRegionalDefectAllocation(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceRegionalDefectAllocation(item: RegionalDefectAllocation): String {
    return cappedInspectionRequestBucket(item.weightedVolume())
}
