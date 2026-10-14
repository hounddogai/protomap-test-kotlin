package com.acme.transport.customs.clearance.clearanceoverride

import com.acme.transport.kit.text.truncate

/** How screens and exports show a clearance draft. */
data class ClearanceDraftView(
    var codeText: String,
    var title: String,
    var graceDaysText: String,
    var fuelLiters: String,
    var versionText: String,
    var batchSizeText: String,
    var statusLabel: String,
)

fun toClearanceDraftView(item: ClearanceDraft): ClearanceDraftView {
    return ClearanceDraftView(
        codeText = truncate(item.code, 120),
        title = item.title,
        graceDaysText = item.graceDays.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        versionText = item.version.toString(),
        batchSizeText = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toClearanceDraftViews(items: MutableList<ClearanceDraft>): MutableList<ClearanceDraftView> {
    val views = mutableListOf<ClearanceDraftView>()
    for (item in items) {
        views.add(toClearanceDraftView(item))
    }
    return views
}

fun sumClearanceDraftView(view: ClearanceDraftView): Double {
    val parsedGraceDays = view.graceDaysText.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedVersion = view.versionText.toInt()
    return parsedGraceDays.toDouble() + parsedFuelLiters + parsedVersion.toDouble()
}
