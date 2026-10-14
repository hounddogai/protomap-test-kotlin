package com.acme.transport.fleetops.tire.tier

import com.acme.transport.fleetops.serviceplan.rollup.ServicePlanDraft
import com.acme.transport.fleetops.serviceplan.rollup.normalizeServicePlanRollupCode

fun summarizeServicePlanDraftsForAgreement(items: MutableList<ServicePlanDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveTireAgreementsFromDraft(items: MutableList<ServicePlanDraft>): MutableList<TireAgreement> {
    val out = mutableListOf<TireAgreement>()
    for (item in items) {
        val derived = newTireAgreement(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveTireAgreement(item: TireAgreement): String = normalizeServicePlanRollupCode(item.code)
