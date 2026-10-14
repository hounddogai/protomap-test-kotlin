package com.acme.transport.terminal.appointment.appointmenttemplate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped appointment template. */
data class CappedAppointmentTemplateView(
    var codeText: String,
    var label: String,
    var activeText: String,
    var currencyText: String,
    var timezone: String,
    var limitText: String,
    var thresholdText: String,
    var lane: String,
    var statusLabel: String,
)

fun toCappedAppointmentTemplateView(item: CappedAppointmentTemplate): CappedAppointmentTemplateView {
    return CappedAppointmentTemplateView(
        codeText = item.code,
        label = truncate(item.label, 80),
        activeText = "${item.active}",
        currencyText = item.currency,
        timezone = item.timezone,
        limitText = item.limit.toString(),
        thresholdText = "%.2f".format(item.threshold),
        lane = item.lane,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedAppointmentTemplateViews(items: MutableList<CappedAppointmentTemplate>): MutableList<CappedAppointmentTemplateView> {
    val views = mutableListOf<CappedAppointmentTemplateView>()
    for (item in items) {
        views.add(toCappedAppointmentTemplateView(item))
    }
    return views
}

fun sumCappedAppointmentTemplateView(view: CappedAppointmentTemplateView): Double {
    val parsedLimit = view.limitText.toInt()
    val parsedThreshold = view.thresholdText.toDouble()
    return parsedLimit.toDouble() + parsedThreshold
}
