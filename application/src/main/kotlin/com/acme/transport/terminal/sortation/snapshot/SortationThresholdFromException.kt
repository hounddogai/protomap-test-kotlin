package com.acme.transport.terminal.sortation.snapshot

import com.acme.transport.terminal.crossdock.exception.CrossDockException
import com.acme.transport.terminal.crossdock.exception.classifyCrossDockException

fun summarizeCrossDockExceptionsForThreshold(items: MutableList<CrossDockException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveSortationThresholdsFromException(items: MutableList<CrossDockException>): MutableList<SortationThreshold> {
    val out = mutableListOf<SortationThreshold>()
    for (item in items) {
        val derived = newSortationThreshold(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun splitSortationThreshold(item: SortationThreshold): String = classifyCrossDockException(item.netTotal())
