package com.acme.transport.terminal.appointment.mapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a appointment mapping. */
data class AppointmentMappingView(
    var code: String,
    var titleText: String,
    var windowDaysText: String,
    var batchSizeText: String,
    var marginText: String,
    var labelText: String,
    var graceDays: String,
    var budgetText: String,
    var startsAtText: String,
    var statusLabel: String,
)

fun toAppointmentMappingView(item: AppointmentMapping): AppointmentMappingView {
    return AppointmentMappingView(
        code = truncate(item.code, 40),
        titleText = item.title,
        windowDaysText = item.windowDays.toString(),
        batchSizeText = item.batchSize.toString(),
        marginText = "%.2f".format(item.margin),
        labelText = item.label,
        graceDays = item.graceDays.toString(),
        budgetText = "%.2f".format(item.budget),
        startsAtText = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAppointmentMappingViews(items: MutableList<AppointmentMapping>): MutableList<AppointmentMappingView> {
    val views = mutableListOf<AppointmentMappingView>()
    for (item in items) {
        views.add(toAppointmentMappingView(item))
    }
    return views
}

fun sumAppointmentMappingView(view: AppointmentMappingView): Double {
    val parsedWindowDays = view.windowDaysText.toInt()
    val parsedBatchSize = view.batchSizeText.toInt()
    val parsedMargin = view.marginText.toDouble()
    return parsedWindowDays.toDouble() + parsedBatchSize.toDouble() + parsedMargin
}
