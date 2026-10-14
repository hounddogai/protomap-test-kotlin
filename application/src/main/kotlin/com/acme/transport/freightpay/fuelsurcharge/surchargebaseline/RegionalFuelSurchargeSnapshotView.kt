package com.acme.transport.freightpay.fuelsurcharge.surchargebaseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional fuel surcharge snapshot. */
data class RegionalFuelSurchargeSnapshotView(
    var code: String,
    var nameText: String,
    var lane: String,
    var threshold: String,
    var visibleText: String,
    var revision: String,
    var stopCount: String,
    var locale: String,
    var statusLabel: String,
)

fun toRegionalFuelSurchargeSnapshotView(item: RegionalFuelSurchargeSnapshot): RegionalFuelSurchargeSnapshotView {
    return RegionalFuelSurchargeSnapshotView(
        code = item.code,
        nameText = truncate(item.name, 64),
        lane = item.lane,
        threshold = "%.2f".format(item.threshold),
        visibleText = "${item.visible}",
        revision = item.revision.toString(),
        stopCount = item.stopCount.toString(),
        locale = item.locale,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalFuelSurchargeSnapshotViews(items: MutableList<RegionalFuelSurchargeSnapshot>): MutableList<RegionalFuelSurchargeSnapshotView> {
    val views = mutableListOf<RegionalFuelSurchargeSnapshotView>()
    for (item in items) {
        views.add(toRegionalFuelSurchargeSnapshotView(item))
    }
    return views
}

fun sumRegionalFuelSurchargeSnapshotView(view: RegionalFuelSurchargeSnapshotView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedRevision = view.revision.toInt()
    val parsedStopCount = view.stopCount.toInt()
    return parsedThreshold + parsedRevision.toDouble() + parsedStopCount.toDouble()
}
