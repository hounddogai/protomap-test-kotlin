package com.acme.transport.emissions.fuelburn.burnmilestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending fuel burn template. */
data class PendingFuelBurnTemplateView(
    var code: String,
    var title: String,
    var endsAt: String,
    var graceDays: String,
    var maximum: String,
    var stopCount: String,
    var countryText: String,
    var capacity: String,
    var archived: String,
    var statusLabel: String,
)

fun toPendingFuelBurnTemplateView(item: PendingFuelBurnTemplate): PendingFuelBurnTemplateView {
    return PendingFuelBurnTemplateView(
        code = truncate(item.code, 64),
        title = item.title,
        endsAt = item.endsAt.toString(),
        graceDays = item.graceDays.toString(),
        maximum = "%.2f".format(item.maximum),
        stopCount = item.stopCount.toString(),
        countryText = item.country,
        capacity = item.capacity.toString(),
        archived = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingFuelBurnTemplateViews(items: MutableList<PendingFuelBurnTemplate>): MutableList<PendingFuelBurnTemplateView> {
    val views = mutableListOf<PendingFuelBurnTemplateView>()
    for (item in items) {
        views.add(toPendingFuelBurnTemplateView(item))
    }
    return views
}

fun sumPendingFuelBurnTemplateView(view: PendingFuelBurnTemplateView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedGraceDays = view.graceDays.toInt()
    val parsedMaximum = view.maximum.toDouble()
    return parsedEndsAt.toDouble() + parsedGraceDays.toDouble() + parsedMaximum
}
