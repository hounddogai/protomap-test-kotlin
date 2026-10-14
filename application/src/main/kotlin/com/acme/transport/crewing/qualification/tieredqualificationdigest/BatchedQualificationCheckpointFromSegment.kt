package com.acme.transport.crewing.qualification.tieredqualificationdigest

import com.acme.transport.crewing.availability.defaultavailabilitysegment.ScheduledAvailabilitySegment
import com.acme.transport.crewing.availability.defaultavailabilitysegment.classifyScheduledAvailabilitySegment

fun summarizeScheduledAvailabilitySegmentsForCheckpoint(items: MutableList<ScheduledAvailabilitySegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveBatchedQualificationCheckpointsFromSegment(items: MutableList<ScheduledAvailabilitySegment>): MutableList<BatchedQualificationCheckpoint> {
    val out = mutableListOf<BatchedQualificationCheckpoint>()
    for (item in items) {
        val derived = newBatchedQualificationCheckpoint(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectBatchedQualificationCheckpoint(item: BatchedQualificationCheckpoint): String {
    return classifyScheduledAvailabilitySegment(item.projectedVolume())
}
