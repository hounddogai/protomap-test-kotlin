package com.acme.transport.fleetops.inspection.segment

/** How screens and exports show a adaptive inspection claim. */
data class AdaptiveInspectionClaimView(
    var code: String,
    var titleText: String,
    var thresholdText: String,
    var reviewer: String,
    var windowDays: String,
    var quantity: String,
    var weight: String,
    var versionText: String,
    var statusLabel: String,
)

fun toAdaptiveInspectionClaimView(item: AdaptiveInspectionClaim): AdaptiveInspectionClaimView {
    return AdaptiveInspectionClaimView(
        code = item.code,
        titleText = item.title,
        thresholdText = "%.2f".format(item.threshold),
        reviewer = item.reviewer,
        windowDays = item.windowDays.toString(),
        quantity = item.quantity.toString(),
        weight = "%.2f".format(item.weight),
        versionText = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveInspectionClaimViews(items: MutableList<AdaptiveInspectionClaim>): MutableList<AdaptiveInspectionClaimView> {
    val views = mutableListOf<AdaptiveInspectionClaimView>()
    for (item in items) {
        views.add(toAdaptiveInspectionClaimView(item))
    }
    return views
}

fun sumAdaptiveInspectionClaimView(view: AdaptiveInspectionClaimView): Double {
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedQuantity = view.quantity.toInt()
    return parsedThreshold + parsedWindowDays.toDouble() + parsedQuantity.toDouble()
}
