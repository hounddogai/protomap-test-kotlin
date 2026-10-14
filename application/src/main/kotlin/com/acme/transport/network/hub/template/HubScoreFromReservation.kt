package com.acme.transport.network.hub.template

import com.acme.transport.network.hub.legacyhubplan.HubReservation
import com.acme.transport.network.hub.legacyhubplan.normalizeManualHubQuotaCode

fun summarizeHubReservationsForScore(items: MutableList<HubReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveHubScoresFromReservation(items: MutableList<HubReservation>): MutableList<HubScore> {
    val out = mutableListOf<HubScore>()
    for (item in items) {
        val derived = newHubScore(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun applyHubScore(item: HubScore): String = normalizeManualHubQuotaCode(item.code)
