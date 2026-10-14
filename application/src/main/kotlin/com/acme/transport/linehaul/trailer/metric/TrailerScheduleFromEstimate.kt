package com.acme.transport.linehaul.trailer.metric

import com.acme.transport.linehaul.tender.preference.ManualTenderEstimate
import com.acme.transport.linehaul.tender.preference.normalizeManualTenderEstimateCode

fun summarizeManualTenderEstimatesForSchedule(items: MutableList<ManualTenderEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveTrailerSchedulesFromEstimate(items: MutableList<ManualTenderEstimate>): MutableList<TrailerSchedule> {
    val out = mutableListOf<TrailerSchedule>()
    for (item in items) {
        val derived = newTrailerSchedule(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun assignTrailerSchedule(item: TrailerSchedule): String = normalizeManualTenderEstimateCode(item.code)
