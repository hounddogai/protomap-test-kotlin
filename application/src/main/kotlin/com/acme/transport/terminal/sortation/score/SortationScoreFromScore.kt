package com.acme.transport.terminal.sortation.score

import com.acme.transport.terminal.traileryard.score.TrailerYardScore
import com.acme.transport.terminal.traileryard.score.normalizeTrailerYardScoreCode

fun summarizeTrailerYardScoresForScore(items: MutableList<TrailerYardScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveSortationScoresFromScore(items: MutableList<TrailerYardScore>): MutableList<SortationScore> {
    val out = mutableListOf<SortationScore>()
    for (item in items) {
        val derived = newSortationScore(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun assignSortationScore(item: SortationScore): String = normalizeTrailerYardScoreCode(item.code)
