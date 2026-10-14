package com.acme.transport.crewing.qualification.qualificationtemplate

import com.acme.transport.crewing.qualification.summary.BatchedQualificationAssignment
import com.acme.transport.crewing.qualification.summary.primaryQualificationOverrideBucket

fun summarizeBatchedQualificationAssignmentsForLedger(items: MutableList<BatchedQualificationAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun derivePrimaryQualificationLedgersFromAssignment(items: MutableList<BatchedQualificationAssignment>): MutableList<PrimaryQualificationLedger> {
    val out = mutableListOf<PrimaryQualificationLedger>()
    for (item in items) {
        val derived = newPrimaryQualificationLedger(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun projectPrimaryQualificationLedger(item: PrimaryQualificationLedger): String {
    return primaryQualificationOverrideBucket(item.adjustedLoad())
}
