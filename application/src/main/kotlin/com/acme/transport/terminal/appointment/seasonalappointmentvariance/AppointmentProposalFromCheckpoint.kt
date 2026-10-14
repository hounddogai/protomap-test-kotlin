package com.acme.transport.terminal.appointment.seasonalappointmentvariance

import com.acme.transport.terminal.dockdoor.checkpoint.CappedDockDoorCheckpoint
import com.acme.transport.terminal.dockdoor.checkpoint.legacyDockDoorDraftBucket

fun summarizeCappedDockDoorCheckpointsForProposal(items: MutableList<CappedDockDoorCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveAppointmentProposalsFromCheckpoint(items: MutableList<CappedDockDoorCheckpoint>): MutableList<AppointmentProposal> {
    val out = mutableListOf<AppointmentProposal>()
    for (item in items) {
        val derived = newAppointmentProposal(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitAppointmentProposal(item: AppointmentProposal): String {
    return legacyDockDoorDraftBucket(item.projectedExposure())
}
