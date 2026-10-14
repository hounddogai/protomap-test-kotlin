package com.acme.transport.crewing.qualification.qualificationproposal

import com.acme.transport.crewing.dutyperiod.periodreview.RollingDutyPeriodForecast
import com.acme.transport.crewing.dutyperiod.periodreview.normalizeFallbackDutyPeriodOverrideCode

fun summarizeRollingDutyPeriodForecastsForAudit(items: MutableList<RollingDutyPeriodForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun derivePendingQualificationAuditsFromForecast(items: MutableList<RollingDutyPeriodForecast>): MutableList<PendingQualificationAudit> {
    val out = mutableListOf<PendingQualificationAudit>()
    for (item in items) {
        val derived = newPendingQualificationAudit(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scorePendingQualificationAudit(item: PendingQualificationAudit): String {
    return normalizeFallbackDutyPeriodOverrideCode(item.code)
}
