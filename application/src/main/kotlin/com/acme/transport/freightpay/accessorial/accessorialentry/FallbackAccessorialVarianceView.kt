package com.acme.transport.freightpay.accessorial.accessorialentry

/** How screens and exports show a fallback accessorial variance. */
data class FallbackAccessorialVarianceView(
    var codeText: String,
    var labelText: String,
    var unit: String,
    var pinned: String,
    var contactEmailText: String,
    var floorValueText: String,
    var startsAtText: String,
    var reviewer: String,
    var archived: String,
    var statusLabel: String,
)

fun toFallbackAccessorialVarianceView(item: FallbackAccessorialVariance): FallbackAccessorialVarianceView {
    return FallbackAccessorialVarianceView(
        codeText = item.code,
        labelText = item.label,
        unit = item.unit,
        pinned = "${item.pinned}",
        contactEmailText = item.contactEmail,
        floorValueText = "%.2f".format(item.floorValue),
        startsAtText = item.startsAt.toString(),
        reviewer = item.reviewer,
        archived = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackAccessorialVarianceViews(items: MutableList<FallbackAccessorialVariance>): MutableList<FallbackAccessorialVarianceView> {
    val views = mutableListOf<FallbackAccessorialVarianceView>()
    for (item in items) {
        views.add(toFallbackAccessorialVarianceView(item))
    }
    return views
}

fun sumFallbackAccessorialVarianceView(view: FallbackAccessorialVarianceView): Double {
    val parsedFloorValue = view.floorValueText.toDouble()
    val parsedStartsAt = view.startsAtText.toInt()
    return parsedFloorValue + parsedStartsAt.toDouble()
}
