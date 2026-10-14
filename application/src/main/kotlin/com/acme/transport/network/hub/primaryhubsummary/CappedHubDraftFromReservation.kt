package com.acme.transport.network.hub.primaryhubsummary

import com.acme.transport.network.cutoff.baseline.GlobalCutoffReservation
import com.acme.transport.network.cutoff.baseline.cutoffBaselineWeight

fun summarizeGlobalCutoffReservationsForDraft(items: MutableList<GlobalCutoffReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveCappedHubDraftsFromReservation(items: MutableList<GlobalCutoffReservation>): MutableList<CappedHubDraft> {
    val out = mutableListOf<CappedHubDraft>()
    for (item in items) {
        val derived = newCappedHubDraft(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun resolveCappedHubDraft(item: CappedHubDraft): Double = cutoffBaselineWeight(9, true)
