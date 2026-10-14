package com.acme.transport.customs.tariff.tariffschedule

import com.acme.transport.customs.broker.brokergroup.SeasonalBrokerOverride
import com.acme.transport.customs.broker.brokergroup.weightedBrokerGroupBucket

fun summarizeSeasonalBrokerOverridesForReservation(items: MutableList<SeasonalBrokerOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveTariffReservationsFromOverride(items: MutableList<SeasonalBrokerOverride>): MutableList<TariffReservation> {
    val out = mutableListOf<TariffReservation>()
    for (item in items) {
        val derived = newTariffReservation(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun mergeTariffReservation(item: TariffReservation): String = weightedBrokerGroupBucket(item.effectiveLoad())
