package com.acme.transport.terminal.sortation.bundle

import com.acme.transport.terminal.dwell.dwelltemplate.DwellException
import com.acme.transport.terminal.dwell.dwelltemplate.classifyRollingDwellTemplate

fun summarizeDwellExceptionsForSegment(items: MutableList<DwellException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveSortationSegmentsFromException(items: MutableList<DwellException>): MutableList<SortationSegment> {
    val out = mutableListOf<SortationSegment>()
    for (item in items) {
        val derived = newSortationSegment(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun resolveSortationSegment(item: SortationSegment): String = classifyRollingDwellTemplate(item.adjustedVolume())
