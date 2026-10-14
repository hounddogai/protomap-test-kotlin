package com.acme.transport.customs.inspection.inspectionentry

/** How screens and exports show a capped inspection entry. */
data class CappedInspectionEntryView(
    var code: String,
    var titleText: String,
    var odometer: String,
    var categoryText: String,
    var thresholdText: String,
    var amountText: String,
    var carrier: String,
    var source: String,
    var version: String,
    var sequence: String,
    var statusLabel: String,
)

fun toCappedInspectionEntryView(item: CappedInspectionEntry): CappedInspectionEntryView {
    return CappedInspectionEntryView(
        code = item.code,
        titleText = item.title,
        odometer = item.odometer.toString(),
        categoryText = item.category,
        thresholdText = "%.2f".format(item.threshold),
        amountText = "%.2f".format(item.amount),
        carrier = item.carrier,
        source = item.source,
        version = item.version.toString(),
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedInspectionEntryViews(items: MutableList<CappedInspectionEntry>): MutableList<CappedInspectionEntryView> {
    val views = mutableListOf<CappedInspectionEntryView>()
    for (item in items) {
        views.add(toCappedInspectionEntryView(item))
    }
    return views
}

fun sumCappedInspectionEntryView(view: CappedInspectionEntryView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedAmount = view.amountText.toDouble()
    return parsedOdometer.toDouble() + parsedThreshold + parsedAmount
}
