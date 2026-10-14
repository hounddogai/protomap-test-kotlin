package com.acme.transport.lastmile.drop.dropslot

import com.acme.transport.lastmile.drop.quota.DropBaseline
import com.acme.transport.lastmile.drop.quota.classifyLenientDropRule

fun summarizeDropBaselinesForEstimate(items: MutableList<DropBaseline>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveDropEstimatesFromBaseline(items: MutableList<DropBaseline>): MutableList<DropEstimate> {
    val out = mutableListOf<DropEstimate>()
    for (item in items) {
        val derived = newDropEstimate(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreDropEstimate(item: DropEstimate): String = classifyLenientDropRule(item.effectiveLoad())
