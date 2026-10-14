package com.acme.transport.fleetops.inspection.inspectionsummary

/** How screens and exports show a capped inspection summary. */
data class CappedInspectionSummaryView(
    var code: String,
    var title: String,
    var discount: String,
    var margin: String,
    var unit: String,
    var statusLabel: String,
)

fun toCappedInspectionSummaryView(item: CappedInspectionSummary): CappedInspectionSummaryView {
    return CappedInspectionSummaryView(
        code = item.code,
        title = item.title,
        discount = "%.2f".format(item.discount),
        margin = "%.2f".format(item.margin),
        unit = item.unit,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedInspectionSummaryViews(items: MutableList<CappedInspectionSummary>): MutableList<CappedInspectionSummaryView> {
    val views = mutableListOf<CappedInspectionSummaryView>()
    for (item in items) {
        views.add(toCappedInspectionSummaryView(item))
    }
    return views
}

fun sumCappedInspectionSummaryView(view: CappedInspectionSummaryView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedMargin = view.margin.toDouble()
    return parsedDiscount + parsedMargin
}
