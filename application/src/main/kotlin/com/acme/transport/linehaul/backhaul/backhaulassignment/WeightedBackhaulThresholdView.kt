package com.acme.transport.linehaul.backhaul.backhaulassignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted backhaul threshold. */
data class WeightedBackhaulThresholdView(
    var code: String,
    var nameText: String,
    var version: String,
    var countryText: String,
    var shiftCode: String,
    var summary: String,
    var approved: String,
    var updatedAt: String,
    var archivedText: String,
    var windowDays: String,
    var revision: String,
    var statusLabel: String,
)

fun toWeightedBackhaulThresholdView(item: WeightedBackhaulThreshold): WeightedBackhaulThresholdView {
    return WeightedBackhaulThresholdView(
        code = item.code,
        nameText = truncate(item.name, 40),
        version = item.version.toString(),
        countryText = item.country,
        shiftCode = item.shiftCode,
        summary = item.summary,
        approved = "${item.approved}",
        updatedAt = item.updatedAt.toString(),
        archivedText = "${item.archived}",
        windowDays = item.windowDays.toString(),
        revision = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedBackhaulThresholdViews(items: MutableList<WeightedBackhaulThreshold>): MutableList<WeightedBackhaulThresholdView> {
    val views = mutableListOf<WeightedBackhaulThresholdView>()
    for (item in items) {
        views.add(toWeightedBackhaulThresholdView(item))
    }
    return views
}

fun sumWeightedBackhaulThresholdView(view: WeightedBackhaulThresholdView): Double {
    val parsedVersion = view.version.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedVersion.toDouble() + parsedUpdatedAt.toDouble() + parsedWindowDays.toDouble()
}
