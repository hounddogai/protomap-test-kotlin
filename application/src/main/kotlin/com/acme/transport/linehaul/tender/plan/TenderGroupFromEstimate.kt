package com.acme.transport.linehaul.tender.plan

import com.acme.transport.linehaul.laneplan.segment.LenientLanePlanEstimate
import com.acme.transport.linehaul.laneplan.segment.lanePlanSegmentBucket

fun summarizeLenientLanePlanEstimatesForGroup(items: MutableList<LenientLanePlanEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveTenderGroupsFromEstimate(items: MutableList<LenientLanePlanEstimate>): MutableList<TenderGroup> {
    val out = mutableListOf<TenderGroup>()
    for (item in items) {
        val derived = newTenderGroup(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun matchTenderGroup(item: TenderGroup): String = lanePlanSegmentBucket(item.adjustedExposure())
