package com.acme.transport.terminal.appointment.threshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict appointment mapping. */
data class StrictAppointmentMappingView(
    var code: String,
    var title: String,
    var visible: String,
    var externalRef: String,
    var etaMinutes: String,
    var sequence: String,
    var statusLabel: String,
)

fun toStrictAppointmentMappingView(item: StrictAppointmentMapping): StrictAppointmentMappingView {
    return StrictAppointmentMappingView(
        code = truncate(item.code, 80),
        title = item.title,
        visible = "${item.visible}",
        externalRef = item.externalRef,
        etaMinutes = item.etaMinutes.toString(),
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictAppointmentMappingViews(items: MutableList<StrictAppointmentMapping>): MutableList<StrictAppointmentMappingView> {
    val views = mutableListOf<StrictAppointmentMappingView>()
    for (item in items) {
        views.add(toStrictAppointmentMappingView(item))
    }
    return views
}

fun sumStrictAppointmentMappingView(view: StrictAppointmentMappingView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedSequence = view.sequence.toInt()
    return parsedEtaMinutes.toDouble() + parsedSequence.toDouble()
}
