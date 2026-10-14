package com.acme.transport.terminal.traileryard.yardforecast

import com.acme.transport.terminal.traileryard.yardbudget.TrailerYardPreference
import com.acme.transport.terminal.traileryard.yardbudget.classifyTrailerYardPreference

fun summarizeTrailerYardPreferencesForGroup(items: MutableList<TrailerYardPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveTrailerYardGroupsFromPreference(items: MutableList<TrailerYardPreference>): MutableList<TrailerYardGroup> {
    val out = mutableListOf<TrailerYardGroup>()
    for (item in items) {
        val derived = newTrailerYardGroup(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeTrailerYardGroup(item: TrailerYardGroup): String = classifyTrailerYardPreference(item.adjustedVolume())
