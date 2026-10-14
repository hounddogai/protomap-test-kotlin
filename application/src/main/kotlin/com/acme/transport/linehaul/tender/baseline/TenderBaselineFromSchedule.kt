package com.acme.transport.linehaul.tender.baseline

import com.acme.transport.linehaul.laneplan.target.LanePlanSchedule
import com.acme.transport.linehaul.laneplan.target.classifyManualLanePlanTarget

fun summarizeLanePlanSchedulesForBaseline(items: MutableList<LanePlanSchedule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveTenderBaselinesFromSchedule(items: MutableList<LanePlanSchedule>): MutableList<TenderBaseline> {
    val out = mutableListOf<TenderBaseline>()
    for (item in items) {
        val derived = newTenderBaseline(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planTenderBaseline(item: TenderBaseline): String = classifyManualLanePlanTarget(item.effectiveExposure())
