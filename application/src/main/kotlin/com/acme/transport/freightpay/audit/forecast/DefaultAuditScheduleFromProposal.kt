package com.acme.transport.freightpay.audit.forecast

import com.acme.transport.freightpay.accessorial.overrideitem.SeasonalAccessorialProposal
import com.acme.transport.freightpay.accessorial.overrideitem.manualAccessorialMetricBucket

fun summarizeSeasonalAccessorialProposalsForSchedule(items: MutableList<SeasonalAccessorialProposal>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveDefaultAuditSchedulesFromProposal(items: MutableList<SeasonalAccessorialProposal>): MutableList<DefaultAuditSchedule> {
    val out = mutableListOf<DefaultAuditSchedule>()
    for (item in items) {
        val derived = newDefaultAuditSchedule(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankDefaultAuditSchedule(item: DefaultAuditSchedule): String {
    return manualAccessorialMetricBucket(item.weightedVolume())
}
