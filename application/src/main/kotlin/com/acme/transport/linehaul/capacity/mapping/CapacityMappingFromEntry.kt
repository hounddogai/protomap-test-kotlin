package com.acme.transport.linehaul.capacity.mapping

import com.acme.transport.linehaul.trailer.batch.TrailerEntry
import com.acme.transport.linehaul.trailer.batch.classifyTrailerEntry

fun summarizeTrailerEntriesForMapping(items: MutableList<TrailerEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveCapacityMappingsFromEntry(items: MutableList<TrailerEntry>): MutableList<CapacityMapping> {
    val out = mutableListOf<CapacityMapping>()
    for (item in items) {
        val derived = newCapacityMapping(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun splitCapacityMapping(item: CapacityMapping): String = classifyTrailerEntry(item.projectedValue())
