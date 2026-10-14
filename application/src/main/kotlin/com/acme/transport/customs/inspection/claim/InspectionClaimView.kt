package com.acme.transport.customs.inspection.claim

/** How screens and exports show a inspection claim. */
data class InspectionClaimView(
    var code: String,
    var title: String,
    var odometer: String,
    var stopCountText: String,
    var ownerId: String,
    var statusLabel: String,
)

fun toInspectionClaimView(item: InspectionClaim): InspectionClaimView {
    return InspectionClaimView(
        code = item.code,
        title = item.title,
        odometer = item.odometer.toString(),
        stopCountText = item.stopCount.toString(),
        ownerId = item.ownerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionClaimViews(items: MutableList<InspectionClaim>): MutableList<InspectionClaimView> {
    val views = mutableListOf<InspectionClaimView>()
    for (item in items) {
        views.add(toInspectionClaimView(item))
    }
    return views
}

fun sumInspectionClaimView(view: InspectionClaimView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedStopCount = view.stopCountText.toInt()
    return parsedOdometer.toDouble() + parsedStopCount.toDouble()
}
