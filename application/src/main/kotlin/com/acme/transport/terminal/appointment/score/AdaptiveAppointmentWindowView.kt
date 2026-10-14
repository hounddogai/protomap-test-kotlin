package com.acme.transport.terminal.appointment.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive appointment window. */
data class AdaptiveAppointmentWindowView(
    var codeText: String,
    var titleText: String,
    var activeText: String,
    var categoryText: String,
    var score: String,
    var statusLabel: String,
)

fun toAdaptiveAppointmentWindowView(item: AdaptiveAppointmentWindow): AdaptiveAppointmentWindowView {
    return AdaptiveAppointmentWindowView(
        codeText = item.code,
        titleText = truncate(item.title, 120),
        activeText = "${item.active}",
        categoryText = item.category,
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveAppointmentWindowViews(items: MutableList<AdaptiveAppointmentWindow>): MutableList<AdaptiveAppointmentWindowView> {
    val views = mutableListOf<AdaptiveAppointmentWindowView>()
    for (item in items) {
        views.add(toAdaptiveAppointmentWindowView(item))
    }
    return views
}

fun sumAdaptiveAppointmentWindowView(view: AdaptiveAppointmentWindowView): Double {
    val parsedScore = view.score.toDouble()
    return parsedScore
}
