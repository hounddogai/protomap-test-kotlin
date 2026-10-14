package com.acme.transport.customs.tariff.preference

import com.acme.transport.customs.broker.checkpoint.CappedBrokerMapping
import com.acme.transport.customs.broker.checkpoint.normalizeCappedBrokerCheckpointCode

fun summarizeCappedBrokerMappingsForClaim(items: MutableList<CappedBrokerMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveCappedTariffClaimsFromMapping(items: MutableList<CappedBrokerMapping>): MutableList<CappedTariffClaim> {
    val out = mutableListOf<CappedTariffClaim>()
    for (item in items) {
        val derived = newCappedTariffClaim(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeCappedTariffClaim(item: CappedTariffClaim): String = normalizeCappedBrokerCheckpointCode(item.code)
