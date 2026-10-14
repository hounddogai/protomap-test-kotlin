package com.acme.transport.terminal.sortation.request

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict sortation manifest. */
data class StrictSortationManifestView(
    var codeText: String,
    var labelText: String,
    var summary: String,
    var phoneText: String,
    var driverId: String,
    var limitText: String,
    var name: String,
    var maximum: String,
    var thresholdText: String,
    var region: String,
    var statusLabel: String,
)

fun toStrictSortationManifestView(item: StrictSortationManifest): StrictSortationManifestView {
    return StrictSortationManifestView(
        codeText = item.code,
        labelText = item.label,
        summary = truncate(item.summary, 80),
        phoneText = item.phone,
        driverId = item.driverId,
        limitText = item.limit.toString(),
        name = item.name,
        maximum = "%.2f".format(item.maximum),
        thresholdText = "%.2f".format(item.threshold),
        region = truncate(item.region, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictSortationManifestViews(items: MutableList<StrictSortationManifest>): MutableList<StrictSortationManifestView> {
    val views = mutableListOf<StrictSortationManifestView>()
    for (item in items) {
        views.add(toStrictSortationManifestView(item))
    }
    return views
}

fun sumStrictSortationManifestView(view: StrictSortationManifestView): Double {
    val parsedLimit = view.limitText.toInt()
    val parsedMaximum = view.maximum.toDouble()
    val parsedThreshold = view.thresholdText.toDouble()
    return parsedLimit.toDouble() + parsedMaximum + parsedThreshold
}
