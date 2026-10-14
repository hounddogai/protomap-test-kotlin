package com.acme.transport.linehaul.backhaul.mapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default backhaul window. */
data class DefaultBackhaulWindowView(
    var codeText: String,
    var labelText: String,
    var updatedAt: String,
    var trailerId: String,
    var durationMinutes: String,
    var descriptionText: String,
    var retries: String,
    var ceiling: String,
    var cost: String,
    var title: String,
    var distanceKmText: String,
    var statusLabel: String,
)

fun toDefaultBackhaulWindowView(item: DefaultBackhaulWindow): DefaultBackhaulWindowView {
    return DefaultBackhaulWindowView(
        codeText = truncate(item.code, 80),
        labelText = item.label,
        updatedAt = item.updatedAt.toString(),
        trailerId = item.trailerId,
        durationMinutes = item.durationMinutes.toString(),
        descriptionText = item.description,
        retries = item.retries.toString(),
        ceiling = "%.2f".format(item.ceiling),
        cost = "%.2f".format(item.cost),
        title = truncate(item.title, 40),
        distanceKmText = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultBackhaulWindowViews(items: MutableList<DefaultBackhaulWindow>): MutableList<DefaultBackhaulWindowView> {
    val views = mutableListOf<DefaultBackhaulWindowView>()
    for (item in items) {
        views.add(toDefaultBackhaulWindowView(item))
    }
    return views
}

fun sumDefaultBackhaulWindowView(view: DefaultBackhaulWindowView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedRetries = view.retries.toInt()
    return parsedUpdatedAt.toDouble() + parsedDurationMinutes.toDouble() + parsedRetries.toDouble()
}
