package com.acme.transport.fleetops.inspection.estimate

/** How screens and exports show a legacy inspection claim. */
data class LegacyInspectionClaimView(
    var codeText: String,
    var title: String,
    var floorValue: String,
    var startsAtText: String,
    var ratio: String,
    var fuelLiters: String,
    var statusLabel: String,
)

fun toLegacyInspectionClaimView(item: LegacyInspectionClaim): LegacyInspectionClaimView {
    return LegacyInspectionClaimView(
        codeText = item.code,
        title = item.title,
        floorValue = "%.2f".format(item.floorValue),
        startsAtText = item.startsAt.toString(),
        ratio = "%.2f".format(item.ratio),
        fuelLiters = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyInspectionClaimViews(items: MutableList<LegacyInspectionClaim>): MutableList<LegacyInspectionClaimView> {
    val views = mutableListOf<LegacyInspectionClaimView>()
    for (item in items) {
        views.add(toLegacyInspectionClaimView(item))
    }
    return views
}

fun sumLegacyInspectionClaimView(view: LegacyInspectionClaimView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedStartsAt = view.startsAtText.toInt()
    val parsedRatio = view.ratio.toDouble()
    return parsedFloorValue + parsedStartsAt.toDouble() + parsedRatio
}
