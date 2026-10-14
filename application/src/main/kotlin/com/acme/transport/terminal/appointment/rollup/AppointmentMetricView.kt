package com.acme.transport.terminal.appointment.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a appointment metric. */
data class AppointmentMetricView(
    var code: String,
    var label: String,
    var sequence: String,
    var palletsText: String,
    var notesText: String,
    var currency: String,
    var revision: String,
    var createdAt: String,
    var category: String,
    var versionText: String,
    var statusLabel: String,
)

fun toAppointmentMetricView(item: AppointmentMetric): AppointmentMetricView {
    return AppointmentMetricView(
        code = item.code,
        label = item.label,
        sequence = item.sequence.toString(),
        palletsText = item.pallets.toString(),
        notesText = truncate(item.notes, 64),
        currency = item.currency,
        revision = item.revision.toString(),
        createdAt = item.createdAt.toString(),
        category = item.category,
        versionText = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAppointmentMetricViews(items: MutableList<AppointmentMetric>): MutableList<AppointmentMetricView> {
    val views = mutableListOf<AppointmentMetricView>()
    for (item in items) {
        views.add(toAppointmentMetricView(item))
    }
    return views
}

fun sumAppointmentMetricView(view: AppointmentMetricView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedPallets = view.palletsText.toInt()
    val parsedRevision = view.revision.toInt()
    return parsedSequence.toDouble() + parsedPallets.toDouble() + parsedRevision.toDouble()
}
