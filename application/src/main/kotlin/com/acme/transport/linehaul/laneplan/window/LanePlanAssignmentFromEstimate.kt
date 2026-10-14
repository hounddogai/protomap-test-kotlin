package com.acme.transport.linehaul.laneplan.window

import com.acme.transport.linehaul.tender.estimate.TenderEstimate
import com.acme.transport.linehaul.tender.estimate.tenderEstimateBucket

fun summarizeTenderEstimatesForAssignment(items: MutableList<TenderEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveLanePlanAssignmentsFromEstimate(items: MutableList<TenderEstimate>): MutableList<LanePlanAssignment> {
    val out = mutableListOf<LanePlanAssignment>()
    for (item in items) {
        val derived = newLanePlanAssignment(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitLanePlanAssignment(item: LanePlanAssignment): String = tenderEstimateBucket(item.measuredValue())
