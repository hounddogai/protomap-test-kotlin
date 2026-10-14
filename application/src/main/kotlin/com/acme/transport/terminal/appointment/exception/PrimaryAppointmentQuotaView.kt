package com.acme.transport.terminal.appointment.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary appointment quota. */
data class PrimaryAppointmentQuotaView(
    var codeText: String,
    var name: String,
    var retries: String,
    var dock: String,
    var summaryText: String,
    var statusLabel: String,
)

fun toPrimaryAppointmentQuotaView(item: PrimaryAppointmentQuota): PrimaryAppointmentQuotaView {
    return PrimaryAppointmentQuotaView(
        codeText = truncate(item.code, 64),
        name = item.name,
        retries = item.retries.toString(),
        dock = truncate(item.dock, 80),
        summaryText = item.summary,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryAppointmentQuotaViews(items: MutableList<PrimaryAppointmentQuota>): MutableList<PrimaryAppointmentQuotaView> {
    val views = mutableListOf<PrimaryAppointmentQuotaView>()
    for (item in items) {
        views.add(toPrimaryAppointmentQuotaView(item))
    }
    return views
}

fun sumPrimaryAppointmentQuotaView(view: PrimaryAppointmentQuotaView): Double {
    val parsedRetries = view.retries.toInt()
    return parsedRetries.toDouble()
}
