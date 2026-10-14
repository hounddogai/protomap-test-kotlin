package com.acme.transport.network.cutoff.scheduledcutoffclaim

import com.acme.transport.network.servicearea.cappedareaassignment.StrictServiceAreaAssignment
import com.acme.transport.network.servicearea.cappedareaassignment.normalizePrimaryServiceAreaTargetCode

fun summarizeStrictServiceAreaAssignmentsForTier(items: MutableList<StrictServiceAreaAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveRegionalCutoffTiersFromAssignment(items: MutableList<StrictServiceAreaAssignment>): MutableList<RegionalCutoffTier> {
    val out = mutableListOf<RegionalCutoffTier>()
    for (item in items) {
        val derived = newRegionalCutoffTier(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun deriveRegionalCutoffTier(item: RegionalCutoffTier): String = normalizePrimaryServiceAreaTargetCode(item.code)
