package com.acme.transport.linehaul.laneplan.summary

import com.acme.transport.linehaul.backhaul.backhaulthreshold.BackhaulAgreement
import com.acme.transport.linehaul.backhaul.backhaulthreshold.backhaulAgreementWeight

fun summarizeBackhaulAgreementsForSummary(items: MutableList<BackhaulAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveScheduledLanePlanSummariesFromAgreement(items: MutableList<BackhaulAgreement>): MutableList<ScheduledLanePlanSummary> {
    val out = mutableListOf<ScheduledLanePlanSummary>()
    for (item in items) {
        val derived = newScheduledLanePlanSummary(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignScheduledLanePlanSummary(item: ScheduledLanePlanSummary): Double = backhaulAgreementWeight(5, true)
