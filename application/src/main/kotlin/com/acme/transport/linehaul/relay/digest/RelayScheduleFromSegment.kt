package com.acme.transport.linehaul.relay.digest

import com.acme.transport.linehaul.laneplan.segment.LanePlanSegment
import com.acme.transport.linehaul.laneplan.segment.classifyLenientLanePlanEstimate

fun summarizeLanePlanSegmentsForSchedule(items: MutableList<LanePlanSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveRelaySchedulesFromSegment(items: MutableList<LanePlanSegment>): MutableList<RelaySchedule> {
    val out = mutableListOf<RelaySchedule>()
    for (item in items) {
        val derived = newRelaySchedule(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun resolveRelaySchedule(item: RelaySchedule): String = classifyLenientLanePlanEstimate(item.weightedExposure())
