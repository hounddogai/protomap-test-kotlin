package com.acme.transport.linehaul.tender.quota

import com.acme.transport.linehaul.trailer.recorditem.CappedTrailerAgreement
import com.acme.transport.linehaul.trailer.recorditem.classifyAdaptiveTrailerAgreement

fun summarizeCappedTrailerAgreementsForSegment(items: MutableList<CappedTrailerAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveTenderSegmentsFromAgreement(items: MutableList<CappedTrailerAgreement>): MutableList<TenderSegment> {
    val out = mutableListOf<TenderSegment>()
    for (item in items) {
        val derived = newTenderSegment(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeTenderSegment(item: TenderSegment): String = classifyAdaptiveTrailerAgreement(item.netValue())
