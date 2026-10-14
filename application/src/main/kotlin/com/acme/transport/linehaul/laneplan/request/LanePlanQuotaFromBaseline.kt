package com.acme.transport.linehaul.laneplan.request

import com.acme.transport.linehaul.tender.baseline.TenderBaseline
import com.acme.transport.linehaul.tender.baseline.classifyTenderTarget

fun summarizeTenderBaselinesForQuota(items: MutableList<TenderBaseline>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveLanePlanQuotasFromBaseline(items: MutableList<TenderBaseline>): MutableList<LanePlanQuota> {
    val out = mutableListOf<LanePlanQuota>()
    for (item in items) {
        val derived = newLanePlanQuota(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileLanePlanQuota(item: LanePlanQuota): String = classifyTenderTarget(item.measuredValue())
