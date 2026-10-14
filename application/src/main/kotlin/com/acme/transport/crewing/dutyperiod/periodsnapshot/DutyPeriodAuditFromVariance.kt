package com.acme.transport.crewing.dutyperiod.periodsnapshot

import com.acme.transport.crewing.restbreak.breakvariance.SeasonalRestBreakVariance
import com.acme.transport.crewing.restbreak.breakvariance.normalizeTieredRestBreakManifestCode

fun summarizeSeasonalRestBreakVariancesForAudit(items: MutableList<SeasonalRestBreakVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveDutyPeriodAuditsFromVariance(items: MutableList<SeasonalRestBreakVariance>): MutableList<DutyPeriodAudit> {
    val out = mutableListOf<DutyPeriodAudit>()
    for (item in items) {
        val derived = newDutyPeriodAudit(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun splitDutyPeriodAudit(item: DutyPeriodAudit): String = normalizeTieredRestBreakManifestCode(item.code)
