package com.acme.transport.network.zone.threshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled zone template. */
data class ScheduledZoneTemplateView(
    var code: String,
    var label: String,
    var windowDays: String,
    var amount: String,
    var checksumText: String,
    var fuelLitersText: String,
    var attempts: String,
    var source: String,
    var statusLabel: String,
)

fun toScheduledZoneTemplateView(item: ScheduledZoneTemplate): ScheduledZoneTemplateView {
    return ScheduledZoneTemplateView(
        code = item.code,
        label = item.label,
        windowDays = item.windowDays.toString(),
        amount = "%.2f".format(item.amount),
        checksumText = truncate(item.checksum, 64),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        attempts = item.attempts.toString(),
        source = item.source,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledZoneTemplateViews(items: MutableList<ScheduledZoneTemplate>): MutableList<ScheduledZoneTemplateView> {
    val views = mutableListOf<ScheduledZoneTemplateView>()
    for (item in items) {
        views.add(toScheduledZoneTemplateView(item))
    }
    return views
}

fun sumScheduledZoneTemplateView(view: ScheduledZoneTemplateView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedAmount = view.amount.toDouble()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    return parsedWindowDays.toDouble() + parsedAmount + parsedFuelLiters
}
