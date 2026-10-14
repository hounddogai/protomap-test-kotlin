package com.acme.transport.crewing.dutyperiod.slot

import com.acme.transport.crewing.qualification.audit.QualificationAudit
import com.acme.transport.crewing.qualification.audit.qualificationForecastWeight

fun summarizeQualificationAuditsForThreshold(items: MutableList<QualificationAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveManualDutyPeriodThresholdsFromAudit(items: MutableList<QualificationAudit>): MutableList<ManualDutyPeriodThreshold> {
    val out = mutableListOf<ManualDutyPeriodThreshold>()
    for (item in items) {
        val derived = newManualDutyPeriodThreshold(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun estimateManualDutyPeriodThreshold(item: ManualDutyPeriodThreshold): Double {
    return qualificationForecastWeight(6, true)
}
