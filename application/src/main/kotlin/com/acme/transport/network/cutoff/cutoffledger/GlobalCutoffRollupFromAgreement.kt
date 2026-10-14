package com.acme.transport.network.cutoff.cutoffledger

import com.acme.transport.network.servicearea.areaassignment.ServiceAreaAgreement
import com.acme.transport.network.servicearea.areaassignment.serviceAreaQuotaWeight

fun summarizeServiceAreaAgreementsForRollup(items: MutableList<ServiceAreaAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveGlobalCutoffRollupsFromAgreement(items: MutableList<ServiceAreaAgreement>): MutableList<GlobalCutoffRollup> {
    val out = mutableListOf<GlobalCutoffRollup>()
    for (item in items) {
        val derived = newGlobalCutoffRollup(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun applyGlobalCutoffRollup(item: GlobalCutoffRollup): Double = serviceAreaQuotaWeight(8, true)
