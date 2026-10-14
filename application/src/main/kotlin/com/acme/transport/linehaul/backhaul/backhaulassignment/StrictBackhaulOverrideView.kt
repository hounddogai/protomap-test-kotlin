package com.acme.transport.linehaul.backhaul.backhaulassignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict backhaul override. */
data class StrictBackhaulOverrideView(
    var codeText: String,
    var title: String,
    var fuelLiters: String,
    var sourceText: String,
    var weight: String,
    var graceDaysText: String,
    var trailerId: String,
    var phoneText: String,
    var locale: String,
    var statusLabel: String,
)

fun toStrictBackhaulOverrideView(item: StrictBackhaulOverride): StrictBackhaulOverrideView {
    return StrictBackhaulOverrideView(
        codeText = truncate(item.code, 64),
        title = item.title,
        fuelLiters = "%.2f".format(item.fuelLiters),
        sourceText = truncate(item.source, 64),
        weight = "%.2f".format(item.weight),
        graceDaysText = item.graceDays.toString(),
        trailerId = item.trailerId,
        phoneText = truncate(item.phone, 120),
        locale = item.locale,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictBackhaulOverrideViews(items: MutableList<StrictBackhaulOverride>): MutableList<StrictBackhaulOverrideView> {
    val views = mutableListOf<StrictBackhaulOverrideView>()
    for (item in items) {
        views.add(toStrictBackhaulOverrideView(item))
    }
    return views
}

fun sumStrictBackhaulOverrideView(view: StrictBackhaulOverrideView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedWeight = view.weight.toDouble()
    val parsedGraceDays = view.graceDaysText.toInt()
    return parsedFuelLiters + parsedWeight + parsedGraceDays.toDouble()
}
