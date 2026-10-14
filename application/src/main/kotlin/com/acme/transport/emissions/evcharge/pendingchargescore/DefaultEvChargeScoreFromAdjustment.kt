package com.acme.transport.emissions.evcharge.pendingchargescore

import com.acme.transport.emissions.carbonreport.strictreportadjustment.PrimaryCarbonReportAdjustment
import com.acme.transport.emissions.carbonreport.strictreportadjustment.normalizePrimaryCarbonReportAdjustmentCode

fun summarizePrimaryCarbonReportAdjustmentsForScore(items: MutableList<PrimaryCarbonReportAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveDefaultEvChargeScoresFromAdjustment(items: MutableList<PrimaryCarbonReportAdjustment>): MutableList<DefaultEvChargeScore> {
    val out = mutableListOf<DefaultEvChargeScore>()
    for (item in items) {
        val derived = newDefaultEvChargeScore(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun scoreDefaultEvChargeScore(item: DefaultEvChargeScore): String {
    return normalizePrimaryCarbonReportAdjustmentCode(item.code)
}
