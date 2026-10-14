package com.acme.transport.linehaul.capacity.capacitypreference

import com.acme.transport.linehaul.laneplan.manifest.ScheduledLanePlanRecord
import com.acme.transport.linehaul.laneplan.manifest.normalizeScheduledLanePlanRecordCode

fun summarizeScheduledLanePlanRecordsForException(items: MutableList<ScheduledLanePlanRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveManualCapacityExceptionsFromRecord(items: MutableList<ScheduledLanePlanRecord>): MutableList<ManualCapacityException> {
    val out = mutableListOf<ManualCapacityException>()
    for (item in items) {
        val derived = newManualCapacityException(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeManualCapacityException(item: ManualCapacityException): String {
    return normalizeScheduledLanePlanRecordCode(item.code)
}
