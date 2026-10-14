package com.acme.transport.network.transittime.manifest

import com.acme.transport.network.zone.zonemapping.LenientZoneAgreement
import com.acme.transport.network.zone.zonemapping.lenientZoneAgreementWeight

fun summarizeLenientZoneAgreementsForNotice(items: MutableList<LenientZoneAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveStrictTransitTimeNoticesFromAgreement(items: MutableList<LenientZoneAgreement>): MutableList<StrictTransitTimeNotice> {
    val out = mutableListOf<StrictTransitTimeNotice>()
    for (item in items) {
        val derived = newStrictTransitTimeNotice(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyStrictTransitTimeNotice(item: StrictTransitTimeNotice): Double = lenientZoneAgreementWeight(8, true)
