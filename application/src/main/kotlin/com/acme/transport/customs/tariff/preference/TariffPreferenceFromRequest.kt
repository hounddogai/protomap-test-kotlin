package com.acme.transport.customs.tariff.preference

import com.acme.transport.customs.clearance.clearancerequest.PrimaryClearanceRequest
import com.acme.transport.customs.clearance.clearancerequest.clearanceDigestBucket

fun summarizePrimaryClearanceRequestsForPreference(items: MutableList<PrimaryClearanceRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveTariffPreferencesFromRequest(items: MutableList<PrimaryClearanceRequest>): MutableList<TariffPreference> {
    val out = mutableListOf<TariffPreference>()
    for (item in items) {
        val derived = newTariffPreference(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankTariffPreference(item: TariffPreference): String = clearanceDigestBucket(item.adjustedExposure())
