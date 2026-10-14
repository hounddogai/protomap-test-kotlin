package com.acme.transport.customs.inspection.mapping

import com.acme.transport.customs.tariff.score.PrimaryTariffRequest
import com.acme.transport.customs.tariff.score.normalizeDefaultTariffDraftCode

fun summarizePrimaryTariffRequestsForMapping(items: MutableList<PrimaryTariffRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveDefaultInspectionMappingsFromRequest(items: MutableList<PrimaryTariffRequest>): MutableList<DefaultInspectionMapping> {
    val out = mutableListOf<DefaultInspectionMapping>()
    for (item in items) {
        val derived = newDefaultInspectionMapping(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun balanceDefaultInspectionMapping(item: DefaultInspectionMapping): String {
    return normalizeDefaultTariffDraftCode(item.code)
}
