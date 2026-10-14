package com.acme.transport.emissions.fuelburn.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual fuel burn threshold. */
data class ManualFuelBurnThresholdView(
    var code: String,
    var title: String,
    var weight: String,
    var dock: String,
    var revision: String,
    var nameText: String,
    var margin: String,
    var phoneText: String,
    var statusLabel: String,
)

fun toManualFuelBurnThresholdView(item: ManualFuelBurnThreshold): ManualFuelBurnThresholdView {
    return ManualFuelBurnThresholdView(
        code = item.code,
        title = truncate(item.title, 80),
        weight = "%.2f".format(item.weight),
        dock = item.dock,
        revision = item.revision.toString(),
        nameText = item.name,
        margin = "%.2f".format(item.margin),
        phoneText = item.phone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualFuelBurnThresholdViews(items: MutableList<ManualFuelBurnThreshold>): MutableList<ManualFuelBurnThresholdView> {
    val views = mutableListOf<ManualFuelBurnThresholdView>()
    for (item in items) {
        views.add(toManualFuelBurnThresholdView(item))
    }
    return views
}

fun sumManualFuelBurnThresholdView(view: ManualFuelBurnThresholdView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedRevision = view.revision.toInt()
    val parsedMargin = view.margin.toDouble()
    return parsedWeight + parsedRevision.toDouble() + parsedMargin
}
