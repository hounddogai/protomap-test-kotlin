package com.acme.transport.crewing.qualification.checkpoint

import com.acme.transport.crewing.qualification.target.QualificationNotice
import com.acme.transport.crewing.qualification.target.normalizeQualificationQuotaCode

fun summarizeQualificationNoticesForCheckpoint(items: MutableList<QualificationNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveQualificationCheckpointsFromNotice(items: MutableList<QualificationNotice>): MutableList<QualificationCheckpoint> {
    val out = mutableListOf<QualificationCheckpoint>()
    for (item in items) {
        val derived = newQualificationCheckpoint(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeQualificationCheckpoint(item: QualificationCheckpoint): String {
    return normalizeQualificationQuotaCode(item.code)
}
