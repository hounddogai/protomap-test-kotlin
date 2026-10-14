package com.acme.transport.linehaul.tender.rollup

import com.acme.transport.linehaul.relay.notice.RelayReservation
import com.acme.transport.linehaul.relay.notice.normalizeRelayRollupCode

fun summarizeRelayReservationsForScore(items: MutableList<RelayReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveTenderScoresFromReservation(items: MutableList<RelayReservation>): MutableList<TenderScore> {
    val out = mutableListOf<TenderScore>()
    for (item in items) {
        val derived = newTenderScore(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun scoreTenderScore(item: TenderScore): String = normalizeRelayRollupCode(item.code)
