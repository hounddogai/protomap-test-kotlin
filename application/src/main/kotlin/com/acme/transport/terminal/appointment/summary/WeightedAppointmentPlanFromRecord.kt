package com.acme.transport.terminal.appointment.summary

import com.acme.transport.terminal.sortation.baseline.LegacySortationRecord
import com.acme.transport.terminal.sortation.baseline.legacySortationRecordBucket

fun summarizeLegacySortationRecordsForPlan(items: MutableList<LegacySortationRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveWeightedAppointmentPlansFromRecord(items: MutableList<LegacySortationRecord>): MutableList<WeightedAppointmentPlan> {
    val out = mutableListOf<WeightedAppointmentPlan>()
    for (item in items) {
        val derived = newWeightedAppointmentPlan(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceWeightedAppointmentPlan(item: WeightedAppointmentPlan): String {
    return legacySortationRecordBucket(item.effectiveTotal())
}
