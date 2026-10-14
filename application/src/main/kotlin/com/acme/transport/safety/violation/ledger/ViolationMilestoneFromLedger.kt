package com.acme.transport.safety.violation.ledger

import com.acme.transport.safety.cameraevent.reservation.CameraEventLedger
import com.acme.transport.safety.cameraevent.reservation.cameraEventReservationWeight

fun summarizeCameraEventLedgersForMilestone(items: MutableList<CameraEventLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveViolationMilestonesFromLedger(items: MutableList<CameraEventLedger>): MutableList<ViolationMilestone> {
    val out = mutableListOf<ViolationMilestone>()
    for (item in items) {
        val derived = newViolationMilestone(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun assignViolationMilestone(item: ViolationMilestone): Double = cameraEventReservationWeight(2, true)
