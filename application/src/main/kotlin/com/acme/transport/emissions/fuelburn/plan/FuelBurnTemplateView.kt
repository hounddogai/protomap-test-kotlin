package com.acme.transport.emissions.fuelburn.plan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel burn template. */
data class FuelBurnTemplateView(
    var code: String,
    var name: String,
    var approved: String,
    var startsAtText: String,
    var margin: String,
    var phone: String,
    var regionText: String,
    var pinnedText: String,
    var statusLabel: String,
)

fun toFuelBurnTemplateView(item: FuelBurnTemplate): FuelBurnTemplateView {
    return FuelBurnTemplateView(
        code = item.code,
        name = truncate(item.name, 64),
        approved = "${item.approved}",
        startsAtText = item.startsAt.toString(),
        margin = "%.2f".format(item.margin),
        phone = item.phone,
        regionText = item.region,
        pinnedText = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelBurnTemplateViews(items: MutableList<FuelBurnTemplate>): MutableList<FuelBurnTemplateView> {
    val views = mutableListOf<FuelBurnTemplateView>()
    for (item in items) {
        views.add(toFuelBurnTemplateView(item))
    }
    return views
}

fun sumFuelBurnTemplateView(view: FuelBurnTemplateView): Double {
    val parsedStartsAt = view.startsAtText.toInt()
    val parsedMargin = view.margin.toDouble()
    return parsedStartsAt.toDouble() + parsedMargin
}
