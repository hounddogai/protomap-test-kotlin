package com.acme.transport.terminal.appointment.bundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a appointment quota. */
data class AppointmentQuotaView(
    var code: String,
    var title: String,
    var sequence: String,
    var pinnedText: String,
    var revision: String,
    var ceiling: String,
    var createdAt: String,
    var statusLabel: String,
)

fun toAppointmentQuotaView(item: AppointmentQuota): AppointmentQuotaView {
    return AppointmentQuotaView(
        code = item.code,
        title = truncate(item.title, 40),
        sequence = item.sequence.toString(),
        pinnedText = "${item.pinned}",
        revision = item.revision.toString(),
        ceiling = "%.2f".format(item.ceiling),
        createdAt = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAppointmentQuotaViews(items: MutableList<AppointmentQuota>): MutableList<AppointmentQuotaView> {
    val views = mutableListOf<AppointmentQuotaView>()
    for (item in items) {
        views.add(toAppointmentQuotaView(item))
    }
    return views
}

fun sumAppointmentQuotaView(view: AppointmentQuotaView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedRevision = view.revision.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedSequence.toDouble() + parsedRevision.toDouble() + parsedCeiling
}
