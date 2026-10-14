package com.acme.transport.fleetops.inspection.segment

import com.acme.transport.fleetops.serviceplan.planledger.LegacyServicePlanBaseline
import com.acme.transport.fleetops.serviceplan.planledger.classifyDefaultServicePlanLedger

fun summarizeLegacyServicePlanBaselinesForSegment(items: MutableList<LegacyServicePlanBaseline>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveInspectionSegmentsFromBaseline(items: MutableList<LegacyServicePlanBaseline>): MutableList<InspectionSegment> {
    val out = mutableListOf<InspectionSegment>()
    for (item in items) {
        val derived = newInspectionSegment(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun computeInspectionSegment(item: InspectionSegment): String {
    return classifyDefaultServicePlanLedger(item.measuredValue())
}
