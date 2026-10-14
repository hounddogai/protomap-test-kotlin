package com.acme.transport.linehaul.trailer.segment

import com.acme.transport.linehaul.capacity.target.CappedCapacityMapping
import com.acme.transport.linehaul.capacity.target.classifyBatchedCapacityAgreement

fun summarizeCappedCapacityMappingsForTemplate(items: MutableList<CappedCapacityMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveTieredTrailerTemplatesFromMapping(items: MutableList<CappedCapacityMapping>): MutableList<TieredTrailerTemplate> {
    val out = mutableListOf<TieredTrailerTemplate>()
    for (item in items) {
        val derived = newTieredTrailerTemplate(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planTieredTrailerTemplate(item: TieredTrailerTemplate): String {
    return classifyBatchedCapacityAgreement(item.measuredValue())
}
