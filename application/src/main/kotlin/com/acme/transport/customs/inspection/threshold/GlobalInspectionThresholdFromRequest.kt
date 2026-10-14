package com.acme.transport.customs.inspection.threshold

import com.acme.transport.customs.tariff.request.LenientTariffRequest
import com.acme.transport.customs.tariff.request.classifyLenientTariffRequest

fun summarizeLenientTariffRequestsForThreshold(items: MutableList<LenientTariffRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveGlobalInspectionThresholdsFromRequest(items: MutableList<LenientTariffRequest>): MutableList<GlobalInspectionThreshold> {
    val out = mutableListOf<GlobalInspectionThreshold>()
    for (item in items) {
        val derived = newGlobalInspectionThreshold(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun deriveGlobalInspectionThreshold(item: GlobalInspectionThreshold): String {
    return classifyLenientTariffRequest(item.measuredValue())
}
