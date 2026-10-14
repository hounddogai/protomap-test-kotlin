package com.acme.transport.terminal.traileryard.yardsummary

import com.acme.transport.terminal.dwell.bundle.AdaptiveDwellEstimate
import com.acme.transport.terminal.dwell.bundle.adaptiveDwellEntryWeight

fun summarizeAdaptiveDwellEstimatesForException(items: MutableList<AdaptiveDwellEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveTieredTrailerYardExceptionsFromEstimate(items: MutableList<AdaptiveDwellEstimate>): MutableList<TieredTrailerYardException> {
    val out = mutableListOf<TieredTrailerYardException>()
    for (item in items) {
        val derived = newTieredTrailerYardException(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeTieredTrailerYardException(item: TieredTrailerYardException): Double = adaptiveDwellEntryWeight(8, true)
