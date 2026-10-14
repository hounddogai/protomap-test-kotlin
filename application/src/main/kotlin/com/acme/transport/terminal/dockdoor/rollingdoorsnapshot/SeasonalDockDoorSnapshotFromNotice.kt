package com.acme.transport.terminal.dockdoor.rollingdoorsnapshot

import com.acme.transport.terminal.appointment.appointmenttemplate.BatchedAppointmentNotice
import com.acme.transport.terminal.appointment.appointmenttemplate.globalAppointmentWindowBucket

fun summarizeBatchedAppointmentNoticesForSnapshot(items: MutableList<BatchedAppointmentNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveSeasonalDockDoorSnapshotsFromNotice(items: MutableList<BatchedAppointmentNotice>): MutableList<SeasonalDockDoorSnapshot> {
    val out = mutableListOf<SeasonalDockDoorSnapshot>()
    for (item in items) {
        val derived = newSeasonalDockDoorSnapshot(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeSeasonalDockDoorSnapshot(item: SeasonalDockDoorSnapshot): String {
    return globalAppointmentWindowBucket(item.measuredValue())
}
