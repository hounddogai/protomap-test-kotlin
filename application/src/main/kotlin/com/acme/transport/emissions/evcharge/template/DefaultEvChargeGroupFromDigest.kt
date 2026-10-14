package com.acme.transport.emissions.evcharge.template

import com.acme.transport.emissions.idling.digest.TieredIdlingDigest
import com.acme.transport.emissions.idling.digest.normalizeScheduledIdlingAuditCode

fun summarizeTieredIdlingDigestsForGroup(items: MutableList<TieredIdlingDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveDefaultEvChargeGroupsFromDigest(items: MutableList<TieredIdlingDigest>): MutableList<DefaultEvChargeGroup> {
    val out = mutableListOf<DefaultEvChargeGroup>()
    for (item in items) {
        val derived = newDefaultEvChargeGroup(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun mergeDefaultEvChargeGroup(item: DefaultEvChargeGroup): String = normalizeScheduledIdlingAuditCode(item.code)
