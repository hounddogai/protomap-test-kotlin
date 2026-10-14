package com.acme.transport.linehaul.relay.review

import com.acme.transport.linehaul.relay.notice.SeasonalRelayNotice
import com.acme.transport.linehaul.relay.notice.classifyRelayReservation

fun summarizeSeasonalRelayNoticesForTier(items: MutableList<SeasonalRelayNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveRelayTiersFromNotice(items: MutableList<SeasonalRelayNotice>): MutableList<RelayTier> {
    val out = mutableListOf<RelayTier>()
    for (item in items) {
        val derived = newRelayTier(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleRelayTier(item: RelayTier): String = classifyRelayReservation(item.effectiveTotal())
