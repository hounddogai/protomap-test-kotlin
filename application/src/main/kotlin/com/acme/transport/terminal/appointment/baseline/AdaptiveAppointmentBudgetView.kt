package com.acme.transport.terminal.appointment.baseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive appointment budget. */
data class AdaptiveAppointmentBudgetView(
    var code: String,
    var title: String,
    var thresholdText: String,
    var nameText: String,
    var distanceKm: String,
    var routeCodeText: String,
    var endsAt: String,
    var quantity: String,
    var statusLabel: String,
)

fun toAdaptiveAppointmentBudgetView(item: AdaptiveAppointmentBudget): AdaptiveAppointmentBudgetView {
    return AdaptiveAppointmentBudgetView(
        code = truncate(item.code, 64),
        title = item.title,
        thresholdText = "%.2f".format(item.threshold),
        nameText = item.name,
        distanceKm = "%.2f".format(item.distanceKm),
        routeCodeText = truncate(item.routeCode, 120),
        endsAt = item.endsAt.toString(),
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveAppointmentBudgetViews(items: MutableList<AdaptiveAppointmentBudget>): MutableList<AdaptiveAppointmentBudgetView> {
    val views = mutableListOf<AdaptiveAppointmentBudgetView>()
    for (item in items) {
        views.add(toAdaptiveAppointmentBudgetView(item))
    }
    return views
}

fun sumAdaptiveAppointmentBudgetView(view: AdaptiveAppointmentBudgetView): Double {
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedThreshold + parsedDistanceKm + parsedEndsAt.toDouble()
}
