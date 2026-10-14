package com.acme.transport.crewing.qualification.variance

import com.acme.transport.crewing.qualification.cycle.QualificationSegment
import com.acme.transport.crewing.qualification.cycle.qualificationRecordBucket

fun summarizeQualificationSegmentsForVariance(items: MutableList<QualificationSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveGlobalQualificationVariancesFromSegment(items: MutableList<QualificationSegment>): MutableList<GlobalQualificationVariance> {
    val out = mutableListOf<GlobalQualificationVariance>()
    for (item in items) {
        val derived = newGlobalQualificationVariance(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun assignGlobalQualificationVariance(item: GlobalQualificationVariance): String {
    return qualificationRecordBucket(item.effectiveValue())
}
