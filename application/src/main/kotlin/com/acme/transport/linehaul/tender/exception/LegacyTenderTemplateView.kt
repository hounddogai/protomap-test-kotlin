package com.acme.transport.linehaul.tender.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy tender template. */
data class LegacyTenderTemplateView(
    var code: String,
    var label: String,
    var rate: String,
    var budget: String,
    var currency: String,
    var driverId: String,
    var odometer: String,
    var marginText: String,
    var segment: String,
    var durationMinutes: String,
    var statusLabel: String,
)

fun toLegacyTenderTemplateView(item: LegacyTenderTemplate): LegacyTenderTemplateView {
    return LegacyTenderTemplateView(
        code = truncate(item.code, 120),
        label = item.label,
        rate = "%.2f".format(item.rate),
        budget = "%.2f".format(item.budget),
        currency = truncate(item.currency, 40),
        driverId = truncate(item.driverId, 120),
        odometer = item.odometer.toString(),
        marginText = "%.2f".format(item.margin),
        segment = truncate(item.segment, 64),
        durationMinutes = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyTenderTemplateViews(items: MutableList<LegacyTenderTemplate>): MutableList<LegacyTenderTemplateView> {
    val views = mutableListOf<LegacyTenderTemplateView>()
    for (item in items) {
        views.add(toLegacyTenderTemplateView(item))
    }
    return views
}

fun sumLegacyTenderTemplateView(view: LegacyTenderTemplateView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedBudget = view.budget.toDouble()
    val parsedOdometer = view.odometer.toInt()
    return parsedRate + parsedBudget + parsedOdometer.toDouble()
}
