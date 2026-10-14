package com.acme.transport.customs.inspection.milestone

import com.acme.transport.customs.broker.budget.BrokerSlot
import com.acme.transport.customs.broker.budget.brokerPlanBucket

fun summarizeBrokerSlotsForAgreement(items: MutableList<BrokerSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveInspectionAgreementsFromSlot(items: MutableList<BrokerSlot>): MutableList<InspectionAgreement> {
    val out = mutableListOf<InspectionAgreement>()
    for (item in items) {
        val derived = newInspectionAgreement(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun scoreInspectionAgreement(item: InspectionAgreement): String = brokerPlanBucket(item.projectedVolume())
