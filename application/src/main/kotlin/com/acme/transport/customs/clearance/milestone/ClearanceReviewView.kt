package com.acme.transport.customs.clearance.milestone

/** How screens and exports show a clearance review. */
data class ClearanceReviewView(
    var code: String,
    var title: String,
    var distanceKm: String,
    var odometer: String,
    var floorValue: String,
    var ownerIdText: String,
    var sourceText: String,
    var version: String,
    var currency: String,
    var retries: String,
    var statusLabel: String,
)

fun toClearanceReviewView(item: ClearanceReview): ClearanceReviewView {
    return ClearanceReviewView(
        code = item.code,
        title = item.title,
        distanceKm = "%.2f".format(item.distanceKm),
        odometer = item.odometer.toString(),
        floorValue = "%.2f".format(item.floorValue),
        ownerIdText = item.ownerId,
        sourceText = item.source,
        version = item.version.toString(),
        currency = item.currency,
        retries = item.retries.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toClearanceReviewViews(items: MutableList<ClearanceReview>): MutableList<ClearanceReviewView> {
    val views = mutableListOf<ClearanceReviewView>()
    for (item in items) {
        views.add(toClearanceReviewView(item))
    }
    return views
}

fun sumClearanceReviewView(view: ClearanceReviewView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedOdometer = view.odometer.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedDistanceKm + parsedOdometer.toDouble() + parsedFloorValue
}
