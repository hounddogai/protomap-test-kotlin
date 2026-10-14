package com.acme.transport.crewing.availability.score

/** How screens and exports show a lenient availability review. */
data class LenientAvailabilityReviewView(
    var code: String,
    var labelText: String,
    var ceiling: String,
    var floorValue: String,
    var etaMinutes: String,
    var statusLabel: String,
)

fun toLenientAvailabilityReviewView(item: LenientAvailabilityReview): LenientAvailabilityReviewView {
    return LenientAvailabilityReviewView(
        code = item.code,
        labelText = item.label,
        ceiling = "%.2f".format(item.ceiling),
        floorValue = "%.2f".format(item.floorValue),
        etaMinutes = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientAvailabilityReviewViews(items: MutableList<LenientAvailabilityReview>): MutableList<LenientAvailabilityReviewView> {
    val views = mutableListOf<LenientAvailabilityReviewView>()
    for (item in items) {
        views.add(toLenientAvailabilityReviewView(item))
    }
    return views
}

fun sumLenientAvailabilityReviewView(view: LenientAvailabilityReviewView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedCeiling + parsedFloorValue + parsedEtaMinutes.toDouble()
}
