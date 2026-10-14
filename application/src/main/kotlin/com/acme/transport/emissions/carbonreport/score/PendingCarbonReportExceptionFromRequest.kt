package com.acme.transport.emissions.carbonreport.score

import com.acme.transport.emissions.emissionfactor.factorsegment.LenientEmissionFactorRequest
import com.acme.transport.emissions.emissionfactor.factorsegment.weightedEmissionFactorBatchWeight

fun summarizeLenientEmissionFactorRequestsForException(items: MutableList<LenientEmissionFactorRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun derivePendingCarbonReportExceptionsFromRequest(items: MutableList<LenientEmissionFactorRequest>): MutableList<PendingCarbonReportException> {
    val out = mutableListOf<PendingCarbonReportException>()
    for (item in items) {
        val derived = newPendingCarbonReportException(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun reconcilePendingCarbonReportException(item: PendingCarbonReportException): Double {
    return weightedEmissionFactorBatchWeight(8, true)
}
