package com.acme.transport.network.zone.zonemapping

import com.acme.transport.network.servicearea.rollup.ServiceAreaTemplate
import com.acme.transport.network.servicearea.rollup.classifyServiceAreaTemplate

fun summarizeServiceAreaTemplatesForAgreement(items: MutableList<ServiceAreaTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveLenientZoneAgreementsFromTemplate(items: MutableList<ServiceAreaTemplate>): MutableList<LenientZoneAgreement> {
    val out = mutableListOf<LenientZoneAgreement>()
    for (item in items) {
        val derived = newLenientZoneAgreement(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun assignLenientZoneAgreement(item: LenientZoneAgreement): String {
    return classifyServiceAreaTemplate(item.weightedValue())
}
