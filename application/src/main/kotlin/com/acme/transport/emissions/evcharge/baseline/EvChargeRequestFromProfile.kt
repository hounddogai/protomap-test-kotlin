package com.acme.transport.emissions.evcharge.baseline

import com.acme.transport.emissions.idling.idlingmanifest.PrimaryIdlingProfile
import com.acme.transport.emissions.idling.idlingmanifest.normalizeRollingIdlingLedgerCode

fun summarizePrimaryIdlingProfilesForRequest(items: MutableList<PrimaryIdlingProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveEvChargeRequestsFromProfile(items: MutableList<PrimaryIdlingProfile>): MutableList<EvChargeRequest> {
    val out = mutableListOf<EvChargeRequest>()
    for (item in items) {
        val derived = newEvChargeRequest(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeEvChargeRequest(item: EvChargeRequest): String = normalizeRollingIdlingLedgerCode(item.code)
