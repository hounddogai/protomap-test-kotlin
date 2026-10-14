package com.acme.transport.terminal.appointment.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback appointment baseline. */
data class FallbackAppointmentBaselineView(
    var codeText: String,
    var title: String,
    var endsAt: String,
    var pinnedText: String,
    var etaMinutes: String,
    var maximumText: String,
    var statusLabel: String,
)

fun toFallbackAppointmentBaselineView(item: FallbackAppointmentBaseline): FallbackAppointmentBaselineView {
    return FallbackAppointmentBaselineView(
        codeText = truncate(item.code, 120),
        title = item.title,
        endsAt = item.endsAt.toString(),
        pinnedText = "${item.pinned}",
        etaMinutes = item.etaMinutes.toString(),
        maximumText = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackAppointmentBaselineViews(items: MutableList<FallbackAppointmentBaseline>): MutableList<FallbackAppointmentBaselineView> {
    val views = mutableListOf<FallbackAppointmentBaselineView>()
    for (item in items) {
        views.add(toFallbackAppointmentBaselineView(item))
    }
    return views
}

fun sumFallbackAppointmentBaselineView(view: FallbackAppointmentBaselineView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedMaximum = view.maximumText.toDouble()
    return parsedEndsAt.toDouble() + parsedEtaMinutes.toDouble() + parsedMaximum
}
