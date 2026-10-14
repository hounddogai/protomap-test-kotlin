package com.acme.transport.fleetops.utilization.cycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled utilization cycle. */
data class ScheduledUtilizationCycleView(
    var code: String,
    var name: String,
    var driverIdText: String,
    var attempts: String,
    var visible: String,
    var checksum: String,
    var label: String,
    var approvedText: String,
    var statusLabel: String,
)

fun toScheduledUtilizationCycleView(item: ScheduledUtilizationCycle): ScheduledUtilizationCycleView {
    return ScheduledUtilizationCycleView(
        code = item.code,
        name = truncate(item.name, 64),
        driverIdText = item.driverId,
        attempts = item.attempts.toString(),
        visible = "${item.visible}",
        checksum = item.checksum,
        label = item.label,
        approvedText = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledUtilizationCycleViews(items: MutableList<ScheduledUtilizationCycle>): MutableList<ScheduledUtilizationCycleView> {
    val views = mutableListOf<ScheduledUtilizationCycleView>()
    for (item in items) {
        views.add(toScheduledUtilizationCycleView(item))
    }
    return views
}

fun sumScheduledUtilizationCycleView(view: ScheduledUtilizationCycleView): Double {
    val parsedAttempts = view.attempts.toInt()
    return parsedAttempts.toDouble()
}
