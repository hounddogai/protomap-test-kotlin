package com.acme.transport.freightpay.fuelsurcharge.audit

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback fuel surcharge milestone. */
data class FallbackFuelSurchargeMilestoneView(
    var code: String,
    var title: String,
    var graceDaysText: String,
    var routeCode: String,
    var threshold: String,
    var phoneText: String,
    var unit: String,
    var distanceKm: String,
    var ceiling: String,
    var statusLabel: String,
)

fun toFallbackFuelSurchargeMilestoneView(item: FallbackFuelSurchargeMilestone): FallbackFuelSurchargeMilestoneView {
    return FallbackFuelSurchargeMilestoneView(
        code = truncate(item.code, 120),
        title = truncate(item.title, 64),
        graceDaysText = item.graceDays.toString(),
        routeCode = item.routeCode,
        threshold = "%.2f".format(item.threshold),
        phoneText = item.phone,
        unit = item.unit,
        distanceKm = "%.2f".format(item.distanceKm),
        ceiling = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackFuelSurchargeMilestoneViews(items: MutableList<FallbackFuelSurchargeMilestone>): MutableList<FallbackFuelSurchargeMilestoneView> {
    val views = mutableListOf<FallbackFuelSurchargeMilestoneView>()
    for (item in items) {
        views.add(toFallbackFuelSurchargeMilestoneView(item))
    }
    return views
}

fun sumFallbackFuelSurchargeMilestoneView(view: FallbackFuelSurchargeMilestoneView): Double {
    val parsedGraceDays = view.graceDaysText.toInt()
    val parsedThreshold = view.threshold.toDouble()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedGraceDays.toDouble() + parsedThreshold + parsedDistanceKm
}
