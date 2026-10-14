package com.acme.transport.crewing.qualification.cycle

import com.acme.transport.crewing.qualification.checkpoint.PendingQualificationAssignment
import com.acme.transport.crewing.qualification.checkpoint.qualificationCheckpointWeight

fun summarizePendingQualificationAssignmentsForRecord(items: MutableList<PendingQualificationAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveQualificationRecordsFromAssignment(items: MutableList<PendingQualificationAssignment>): MutableList<QualificationRecord> {
    val out = mutableListOf<QualificationRecord>()
    for (item in items) {
        val derived = newQualificationRecord(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyQualificationRecord(item: QualificationRecord): Double = qualificationCheckpointWeight(2, true)
