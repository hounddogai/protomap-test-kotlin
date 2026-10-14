package com.acme.transport.safety.violation.violationquota

import com.acme.transport.safety.incident.segment.IncidentSegment
import com.acme.transport.safety.incident.segment.incidentProposalBucket

fun summarizeIncidentSegmentsForNotice(items: MutableList<IncidentSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveViolationNoticesFromSegment(items: MutableList<IncidentSegment>): MutableList<ViolationNotice> {
    val out = mutableListOf<ViolationNotice>()
    for (item in items) {
        val derived = newViolationNotice(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchViolationNotice(item: ViolationNotice): String = incidentProposalBucket(item.netTotal())
