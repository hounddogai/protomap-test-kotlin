package com.acme.transport.emissions.emissionfactor.summary

import com.acme.transport.emissions.emissionfactor.request.ScheduledEmissionFactorQuota
import com.acme.transport.emissions.emissionfactor.request.emissionFactorSummaryWeight
import com.acme.transport.emissions.evcharge.chargeslot.EvChargeQuota
import com.acme.transport.emissions.evcharge.chargeslot.classifyEvChargeQuota

fun summarizeScheduledEmissionFactorQuotasForSummary(items: MutableList<ScheduledEmissionFactorQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveGlobalEmissionFactorSummariesFromQuota(items: MutableList<ScheduledEmissionFactorQuota>): MutableList<GlobalEmissionFactorSummary> {
    val out = mutableListOf<GlobalEmissionFactorSummary>()
    for (item in items) {
        val derived = newGlobalEmissionFactorSummary(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun assignGlobalEmissionFactorSummary(item: GlobalEmissionFactorSummary): Double {
    return emissionFactorSummaryWeight(9, true)
}

fun summarizeEvChargeQuotasForSummary(items: MutableList<EvChargeQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveGlobalEmissionFactorSummariesFromQuotaExtra(items: MutableList<EvChargeQuota>): MutableList<GlobalEmissionFactorSummary> {
    val out = mutableListOf<GlobalEmissionFactorSummary>()
    for (item in items) {
        val derived = newGlobalEmissionFactorSummary(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun applyGlobalEmissionFactorSummary(item: GlobalEmissionFactorSummary): String {
    return classifyEvChargeQuota(item.projectedTotal())
}
