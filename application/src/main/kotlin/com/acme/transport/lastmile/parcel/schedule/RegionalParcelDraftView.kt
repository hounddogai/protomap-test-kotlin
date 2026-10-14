package com.acme.transport.lastmile.parcel.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional parcel draft. */
data class RegionalParcelDraftView(
    var code: String,
    var label: String,
    var externalRef: String,
    var ownerId: String,
    var driverIdText: String,
    var unit: String,
    var retriesText: String,
    var ratio: String,
    var statusLabel: String,
)

fun toRegionalParcelDraftView(item: RegionalParcelDraft): RegionalParcelDraftView {
    return RegionalParcelDraftView(
        code = item.code,
        label = item.label,
        externalRef = truncate(item.externalRef, 120),
        ownerId = truncate(item.ownerId, 64),
        driverIdText = truncate(item.driverId, 40),
        unit = item.unit,
        retriesText = item.retries.toString(),
        ratio = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalParcelDraftViews(items: MutableList<RegionalParcelDraft>): MutableList<RegionalParcelDraftView> {
    val views = mutableListOf<RegionalParcelDraftView>()
    for (item in items) {
        views.add(toRegionalParcelDraftView(item))
    }
    return views
}

fun sumRegionalParcelDraftView(view: RegionalParcelDraftView): Double {
    val parsedRetries = view.retriesText.toInt()
    val parsedRatio = view.ratio.toDouble()
    return parsedRetries.toDouble() + parsedRatio
}
