package com.acme.transport.emissions.routeoffset.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual route offset variance. */
data class ManualRouteOffsetVarianceView(
    var codeText: String,
    var label: String,
    var archived: String,
    var ratioText: String,
    var odometerText: String,
    var retries: String,
    var revision: String,
    var statusLabel: String,
)

fun toManualRouteOffsetVarianceView(item: ManualRouteOffsetVariance): ManualRouteOffsetVarianceView {
    return ManualRouteOffsetVarianceView(
        codeText = item.code,
        label = truncate(item.label, 40),
        archived = "${item.archived}",
        ratioText = "%.2f".format(item.ratio),
        odometerText = item.odometer.toString(),
        retries = item.retries.toString(),
        revision = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualRouteOffsetVarianceViews(items: MutableList<ManualRouteOffsetVariance>): MutableList<ManualRouteOffsetVarianceView> {
    val views = mutableListOf<ManualRouteOffsetVarianceView>()
    for (item in items) {
        views.add(toManualRouteOffsetVarianceView(item))
    }
    return views
}

fun sumManualRouteOffsetVarianceView(view: ManualRouteOffsetVarianceView): Double {
    val parsedRatio = view.ratioText.toDouble()
    val parsedOdometer = view.odometerText.toInt()
    val parsedRetries = view.retries.toInt()
    return parsedRatio + parsedOdometer.toDouble() + parsedRetries.toDouble()
}
