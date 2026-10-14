package com.acme.transport.network.cutoff.template

import com.acme.transport.network.servicearea.areaclaim.ServiceAreaPlan
import com.acme.transport.network.servicearea.areaclaim.weightedServiceAreaClaimWeight

fun summarizeServiceAreaPlansForTemplate(items: MutableList<ServiceAreaPlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveLenientCutoffTemplatesFromPlan(items: MutableList<ServiceAreaPlan>): MutableList<LenientCutoffTemplate> {
    val out = mutableListOf<LenientCutoffTemplate>()
    for (item in items) {
        val derived = newLenientCutoffTemplate(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeLenientCutoffTemplate(item: LenientCutoffTemplate): Double = weightedServiceAreaClaimWeight(9, true)
