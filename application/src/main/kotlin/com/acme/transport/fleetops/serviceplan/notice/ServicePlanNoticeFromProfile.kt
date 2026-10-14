package com.acme.transport.fleetops.serviceplan.notice

import com.acme.transport.fleetops.defect.policy.BatchedDefectProfile
import com.acme.transport.fleetops.defect.policy.classifyStrictDefectSlot

fun summarizeBatchedDefectProfilesForNotice(items: MutableList<BatchedDefectProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveServicePlanNoticesFromProfile(items: MutableList<BatchedDefectProfile>): MutableList<ServicePlanNotice> {
    val out = mutableListOf<ServicePlanNotice>()
    for (item in items) {
        val derived = newServicePlanNotice(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun applyServicePlanNotice(item: ServicePlanNotice): String = classifyStrictDefectSlot(item.projectedExposure())
