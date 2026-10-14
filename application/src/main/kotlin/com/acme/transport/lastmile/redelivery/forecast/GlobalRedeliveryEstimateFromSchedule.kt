package com.acme.transport.lastmile.redelivery.forecast

import com.acme.transport.lastmile.parcel.schedule.ParcelSchedule
import com.acme.transport.lastmile.parcel.schedule.classifyRegionalParcelDraft

fun summarizeParcelSchedulesForEstimate(items: MutableList<ParcelSchedule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveGlobalRedeliveryEstimatesFromSchedule(items: MutableList<ParcelSchedule>): MutableList<GlobalRedeliveryEstimate> {
    val out = mutableListOf<GlobalRedeliveryEstimate>()
    for (item in items) {
        val derived = newGlobalRedeliveryEstimate(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun classifyGlobalRedeliveryEstimate(item: GlobalRedeliveryEstimate): String {
    return classifyRegionalParcelDraft(item.weightedExposure())
}
