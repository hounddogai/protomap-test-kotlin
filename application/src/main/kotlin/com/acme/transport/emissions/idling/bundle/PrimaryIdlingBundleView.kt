package com.acme.transport.emissions.idling.bundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary idling bundle. */
data class PrimaryIdlingBundleView(
    var code: String,
    var label: String,
    var category: String,
    var summary: String,
    var titleText: String,
    var timezone: String,
    var fuelLitersText: String,
    var startsAt: String,
    var statusLabel: String,
)

fun toPrimaryIdlingBundleView(item: PrimaryIdlingBundle): PrimaryIdlingBundleView {
    return PrimaryIdlingBundleView(
        code = truncate(item.code, 120),
        label = truncate(item.label, 80),
        category = item.category,
        summary = item.summary,
        titleText = item.title,
        timezone = truncate(item.timezone, 80),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        startsAt = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryIdlingBundleViews(items: MutableList<PrimaryIdlingBundle>): MutableList<PrimaryIdlingBundleView> {
    val views = mutableListOf<PrimaryIdlingBundleView>()
    for (item in items) {
        views.add(toPrimaryIdlingBundleView(item))
    }
    return views
}

fun sumPrimaryIdlingBundleView(view: PrimaryIdlingBundleView): Double {
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedFuelLiters + parsedStartsAt.toDouble()
}
