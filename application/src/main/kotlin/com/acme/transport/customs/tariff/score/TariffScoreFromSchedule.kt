package com.acme.transport.customs.tariff.score

import com.acme.transport.customs.inspection.claim.InspectionSchedule
import com.acme.transport.customs.inspection.claim.inspectionScheduleBucket

fun summarizeInspectionSchedulesForScore(items: MutableList<InspectionSchedule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveTariffScoresFromSchedule(items: MutableList<InspectionSchedule>): MutableList<TariffScore> {
    val out = mutableListOf<TariffScore>()
    for (item in items) {
        val derived = newTariffScore(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planTariffScore(item: TariffScore): String = inspectionScheduleBucket(item.measuredValue())
