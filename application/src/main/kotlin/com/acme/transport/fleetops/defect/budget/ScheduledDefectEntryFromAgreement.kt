package com.acme.transport.fleetops.defect.budget

import com.acme.transport.fleetops.serviceplan.agreement.SeasonalServicePlanAgreement
import com.acme.transport.fleetops.serviceplan.agreement.manualServicePlanSegmentWeight

fun summarizeSeasonalServicePlanAgreementsForEntry(items: MutableList<SeasonalServicePlanAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveScheduledDefectEntriesFromAgreement(items: MutableList<SeasonalServicePlanAgreement>): MutableList<ScheduledDefectEntry> {
    val out = mutableListOf<ScheduledDefectEntry>()
    for (item in items) {
        val derived = newScheduledDefectEntry(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun splitScheduledDefectEntry(item: ScheduledDefectEntry): Double = manualServicePlanSegmentWeight(7, true)
