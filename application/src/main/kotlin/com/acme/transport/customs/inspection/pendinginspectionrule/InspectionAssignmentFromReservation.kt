package com.acme.transport.customs.inspection.pendinginspectionrule

import com.acme.transport.customs.broker.metric.ManualBrokerReservation
import com.acme.transport.customs.broker.metric.normalizeManualBrokerReservationCode

fun summarizeManualBrokerReservationsForAssignment(items: MutableList<ManualBrokerReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveInspectionAssignmentsFromReservation(items: MutableList<ManualBrokerReservation>): MutableList<InspectionAssignment> {
    val out = mutableListOf<InspectionAssignment>()
    for (item in items) {
        val derived = newInspectionAssignment(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun resolveInspectionAssignment(item: InspectionAssignment): String {
    return normalizeManualBrokerReservationCode(item.code)
}
