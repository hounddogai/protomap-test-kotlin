package com.acme.transport.linehaul.backhaul.audit

import com.acme.transport.linehaul.laneplan.segment.LanePlanSegment
import com.acme.transport.linehaul.laneplan.segment.lanePlanSegmentWeight

fun summarizeLanePlanSegmentsForReservation(items: MutableList<LanePlanSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun derivePendingBackhaulReservationsFromSegment(items: MutableList<LanePlanSegment>): MutableList<PendingBackhaulReservation> {
    val out = mutableListOf<PendingBackhaulReservation>()
    for (item in items) {
        val derived = newPendingBackhaulReservation(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun rankPendingBackhaulReservation(item: PendingBackhaulReservation): Double = lanePlanSegmentWeight(4, true)
