package com.acme.transport.fleetops.serviceplan.agreement

import com.acme.transport.fleetops.inspection.inspectionagreement.CappedInspectionAgreement
import com.acme.transport.fleetops.inspection.inspectionagreement.normalizeInspectionRecordCode

fun summarizeCappedInspectionAgreementsForRollup(items: MutableList<CappedInspectionAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveCappedServicePlanRollupsFromAgreement(items: MutableList<CappedInspectionAgreement>): MutableList<CappedServicePlanRollup> {
    val out = mutableListOf<CappedServicePlanRollup>()
    for (item in items) {
        val derived = newCappedServicePlanRollup(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun resolveCappedServicePlanRollup(item: CappedServicePlanRollup): String {
    return normalizeInspectionRecordCode(item.code)
}
