package com.acme.transport.linehaul.tender.baseline

import com.acme.transport.linehaul.trailer.rollup.ManualTrailerSegment
import com.acme.transport.linehaul.trailer.rollup.classifyTrailerQuota

fun summarizeManualTrailerSegmentsForBaseline(items: MutableList<ManualTrailerSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveTenderBaselinesFromSegment(items: MutableList<ManualTrailerSegment>): MutableList<TenderBaseline> {
    val out = mutableListOf<TenderBaseline>()
    for (item in items) {
        val derived = newTenderBaseline(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchTenderBaseline(item: TenderBaseline): String = classifyTrailerQuota(item.effectiveExposure())
