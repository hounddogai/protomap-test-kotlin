package com.acme.transport.crewing.restbreak.tier

import com.acme.transport.crewing.restbreak.assignment.RestBreakOverride
import com.acme.transport.crewing.restbreak.assignment.classifyRestBreakAssignment

fun summarizeRestBreakOverridesForNotice(items: MutableList<RestBreakOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveCappedRestBreakNoticesFromOverride(items: MutableList<RestBreakOverride>): MutableList<CappedRestBreakNotice> {
    val out = mutableListOf<CappedRestBreakNotice>()
    for (item in items) {
        val derived = newCappedRestBreakNotice(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun rankCappedRestBreakNotice(item: CappedRestBreakNotice): String {
    return classifyRestBreakAssignment(item.projectedLoad())
}
