package com.acme.transport.linehaul.tender.tenderrollup

import com.acme.transport.linehaul.backhaul.template.BackhaulTemplate
import com.acme.transport.linehaul.backhaul.template.classifyBackhaulForecast

fun summarizeBackhaulTemplatesForVariance(items: MutableList<BackhaulTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveLegacyTenderVariancesFromTemplate(items: MutableList<BackhaulTemplate>): MutableList<LegacyTenderVariance> {
    val out = mutableListOf<LegacyTenderVariance>()
    for (item in items) {
        val derived = newLegacyTenderVariance(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun rankLegacyTenderVariance(item: LegacyTenderVariance): String = classifyBackhaulForecast(item.netVolume())
