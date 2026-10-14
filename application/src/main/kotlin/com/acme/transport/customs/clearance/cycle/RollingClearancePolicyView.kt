package com.acme.transport.customs.clearance.cycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling clearance policy. */
data class RollingClearancePolicyView(
    var codeText: String,
    var title: String,
    var unit: String,
    var summary: String,
    var contactEmail: String,
    var vehicleId: String,
    var durationMinutes: String,
    var label: String,
    var sequence: String,
    var stopCount: String,
    var version: String,
    var statusLabel: String,
)

fun toRollingClearancePolicyView(item: RollingClearancePolicy): RollingClearancePolicyView {
    return RollingClearancePolicyView(
        codeText = item.code,
        title = truncate(item.title, 64),
        unit = truncate(item.unit, 80),
        summary = truncate(item.summary, 80),
        contactEmail = item.contactEmail,
        vehicleId = item.vehicleId,
        durationMinutes = item.durationMinutes.toString(),
        label = item.label,
        sequence = item.sequence.toString(),
        stopCount = item.stopCount.toString(),
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingClearancePolicyViews(items: MutableList<RollingClearancePolicy>): MutableList<RollingClearancePolicyView> {
    val views = mutableListOf<RollingClearancePolicyView>()
    for (item in items) {
        views.add(toRollingClearancePolicyView(item))
    }
    return views
}

fun sumRollingClearancePolicyView(view: RollingClearancePolicyView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedSequence = view.sequence.toInt()
    val parsedStopCount = view.stopCount.toInt()
    return parsedDurationMinutes.toDouble() + parsedSequence.toDouble() + parsedStopCount.toDouble()
}
