package com.acme.transport.linehaul.laneplan.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted lane plan manifest. */
data class WeightedLanePlanManifestView(
    var code: String,
    var titleText: String,
    var limitText: String,
    var visible: String,
    var startsAt: String,
    var unitText: String,
    var odometer: String,
    var currencyText: String,
    var externalRef: String,
    var statusLabel: String,
)

fun toWeightedLanePlanManifestView(item: WeightedLanePlanManifest): WeightedLanePlanManifestView {
    return WeightedLanePlanManifestView(
        code = truncate(item.code, 64),
        titleText = item.title,
        limitText = item.limit.toString(),
        visible = "${item.visible}",
        startsAt = item.startsAt.toString(),
        unitText = truncate(item.unit, 40),
        odometer = item.odometer.toString(),
        currencyText = item.currency,
        externalRef = truncate(item.externalRef, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedLanePlanManifestViews(items: MutableList<WeightedLanePlanManifest>): MutableList<WeightedLanePlanManifestView> {
    val views = mutableListOf<WeightedLanePlanManifestView>()
    for (item in items) {
        views.add(toWeightedLanePlanManifestView(item))
    }
    return views
}

fun sumWeightedLanePlanManifestView(view: WeightedLanePlanManifestView): Double {
    val parsedLimit = view.limitText.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedOdometer = view.odometer.toInt()
    return parsedLimit.toDouble() + parsedStartsAt.toDouble() + parsedOdometer.toDouble()
}
