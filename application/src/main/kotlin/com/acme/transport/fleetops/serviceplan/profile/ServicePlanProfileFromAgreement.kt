package com.acme.transport.fleetops.serviceplan.profile

import com.acme.transport.fleetops.defect.agreement.ManualDefectAgreement
import com.acme.transport.fleetops.defect.agreement.normalizeManualDefectAgreementCode

fun summarizeManualDefectAgreementsForProfile(items: MutableList<ManualDefectAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveServicePlanProfilesFromAgreement(items: MutableList<ManualDefectAgreement>): MutableList<ServicePlanProfile> {
    val out = mutableListOf<ServicePlanProfile>()
    for (item in items) {
        val derived = newServicePlanProfile(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun balanceServicePlanProfile(item: ServicePlanProfile): String = normalizeManualDefectAgreementCode(item.code)
