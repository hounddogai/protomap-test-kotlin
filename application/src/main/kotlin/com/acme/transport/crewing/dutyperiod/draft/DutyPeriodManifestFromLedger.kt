package com.acme.transport.crewing.dutyperiod.draft

import com.acme.transport.crewing.qualification.variance.QualificationLedger
import com.acme.transport.crewing.qualification.variance.normalizeGlobalQualificationVarianceCode

fun summarizeQualificationLedgersForManifest(items: MutableList<QualificationLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveDutyPeriodManifestsFromLedger(items: MutableList<QualificationLedger>): MutableList<DutyPeriodManifest> {
    val out = mutableListOf<DutyPeriodManifest>()
    for (item in items) {
        val derived = newDutyPeriodManifest(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeDutyPeriodManifest(item: DutyPeriodManifest): String {
    return normalizeGlobalQualificationVarianceCode(item.code)
}
