package com.acme.transport.terminal.appointment.draft

/** How screens and exports show a default appointment score. */
data class DefaultAppointmentScoreView(
    var code: String,
    var label: String,
    var etaMinutes: String,
    var carrier: String,
    var visible: String,
    var title: String,
    var statusLabel: String,
)

fun toDefaultAppointmentScoreView(item: DefaultAppointmentScore): DefaultAppointmentScoreView {
    return DefaultAppointmentScoreView(
        code = item.code,
        label = item.label,
        etaMinutes = item.etaMinutes.toString(),
        carrier = item.carrier,
        visible = "${item.visible}",
        title = item.title,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultAppointmentScoreViews(items: MutableList<DefaultAppointmentScore>): MutableList<DefaultAppointmentScoreView> {
    val views = mutableListOf<DefaultAppointmentScoreView>()
    for (item in items) {
        views.add(toDefaultAppointmentScoreView(item))
    }
    return views
}

fun sumDefaultAppointmentScoreView(view: DefaultAppointmentScoreView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedEtaMinutes.toDouble()
}
