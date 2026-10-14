package com.acme.transport.terminal.dwell.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled dwell template. */
data class ScheduledDwellTemplateView(
    var code: String,
    var titleText: String,
    var checksum: String,
    var shiftCode: String,
    var fuelLiters: String,
    var loadKg: String,
    var currencyText: String,
    var budget: String,
    var contactEmail: String,
    var statusLabel: String,
)

fun toScheduledDwellTemplateView(item: ScheduledDwellTemplate): ScheduledDwellTemplateView {
    return ScheduledDwellTemplateView(
        code = item.code,
        titleText = item.title,
        checksum = item.checksum,
        shiftCode = truncate(item.shiftCode, 64),
        fuelLiters = "%.2f".format(item.fuelLiters),
        loadKg = "%.2f".format(item.loadKg),
        currencyText = item.currency,
        budget = "%.2f".format(item.budget),
        contactEmail = truncate(item.contactEmail, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledDwellTemplateViews(items: MutableList<ScheduledDwellTemplate>): MutableList<ScheduledDwellTemplateView> {
    val views = mutableListOf<ScheduledDwellTemplateView>()
    for (item in items) {
        views.add(toScheduledDwellTemplateView(item))
    }
    return views
}

fun sumScheduledDwellTemplateView(view: ScheduledDwellTemplateView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedBudget = view.budget.toDouble()
    return parsedFuelLiters + parsedLoadKg + parsedBudget
}
