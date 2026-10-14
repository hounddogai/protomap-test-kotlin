package com.acme.transport.fleetops.defect.target

import com.acme.transport.fleetops.inspection.policy.InspectionBundle
import com.acme.transport.fleetops.inspection.policy.inspectionPolicyWeight

fun summarizeInspectionBundlesForAgreement(items: MutableList<InspectionBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveDefectAgreementsFromBundle(items: MutableList<InspectionBundle>): MutableList<DefectAgreement> {
    val out = mutableListOf<DefectAgreement>()
    for (item in items) {
        val derived = newDefectAgreement(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun computeDefectAgreement(item: DefectAgreement): Double = inspectionPolicyWeight(3, true)
