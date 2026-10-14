package com.acme.transport.linehaul.trailer.score

import com.acme.transport.linehaul.trailer.regionaltrailerrecord.CappedTrailerRecord
import com.acme.transport.linehaul.trailer.regionaltrailerrecord.manualTrailerTargetWeight

fun summarizeCappedTrailerRecordsForRule(items: MutableList<CappedTrailerRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveCappedTrailerRulesFromRecord(items: MutableList<CappedTrailerRecord>): MutableList<CappedTrailerRule> {
    val out = mutableListOf<CappedTrailerRule>()
    for (item in items) {
        val derived = newCappedTrailerRule(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeCappedTrailerRule(item: CappedTrailerRule): Double = manualTrailerTargetWeight(2, true)
