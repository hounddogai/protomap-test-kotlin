package com.acme.transport.emissions.evcharge.baseline

import com.acme.transport.emissions.carbonreport.reportagreement.LenientCarbonReportManifest
import com.acme.transport.emissions.carbonreport.reportagreement.fallbackCarbonReportAgreementWeight

fun summarizeLenientCarbonReportManifestsForBaseline(items: MutableList<LenientCarbonReportManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveEvChargeBaselinesFromManifest(items: MutableList<LenientCarbonReportManifest>): MutableList<EvChargeBaseline> {
    val out = mutableListOf<EvChargeBaseline>()
    for (item in items) {
        val derived = newEvChargeBaseline(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeEvChargeBaseline(item: EvChargeBaseline): Double = fallbackCarbonReportAgreementWeight(9, true)
