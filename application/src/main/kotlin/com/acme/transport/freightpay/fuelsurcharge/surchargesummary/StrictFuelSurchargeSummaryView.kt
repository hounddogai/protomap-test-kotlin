package com.acme.transport.freightpay.fuelsurcharge.surchargesummary

/** How screens and exports show a strict fuel surcharge summary. */
data class StrictFuelSurchargeSummaryView(
    var codeText: String,
    var title: String,
    var archived: String,
    var priority: String,
    var labelText: String,
    var distanceKm: String,
    var country: String,
    var statusLabel: String,
)

fun toStrictFuelSurchargeSummaryView(item: StrictFuelSurchargeSummary): StrictFuelSurchargeSummaryView {
    return StrictFuelSurchargeSummaryView(
        codeText = item.code,
        title = item.title,
        archived = "${item.archived}",
        priority = item.priority.toString(),
        labelText = item.label,
        distanceKm = "%.2f".format(item.distanceKm),
        country = item.country,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictFuelSurchargeSummaryViews(items: MutableList<StrictFuelSurchargeSummary>): MutableList<StrictFuelSurchargeSummaryView> {
    val views = mutableListOf<StrictFuelSurchargeSummaryView>()
    for (item in items) {
        views.add(toStrictFuelSurchargeSummaryView(item))
    }
    return views
}

fun sumStrictFuelSurchargeSummaryView(view: StrictFuelSurchargeSummaryView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedPriority.toDouble() + parsedDistanceKm
}
