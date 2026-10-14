package com.acme.transport.network.cutoff.draft

import com.acme.transport.network.cutoff.variance.CutoffRollup
import com.acme.transport.network.cutoff.variance.classifyCutoffRollup

fun summarizeCutoffRollupsForSegment(items: MutableList<CutoffRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveLenientCutoffSegmentsFromRollup(items: MutableList<CutoffRollup>): MutableList<LenientCutoffSegment> {
    val out = mutableListOf<LenientCutoffSegment>()
    for (item in items) {
        val derived = newLenientCutoffSegment(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectLenientCutoffSegment(item: LenientCutoffSegment): String = classifyCutoffRollup(item.measuredValue())
