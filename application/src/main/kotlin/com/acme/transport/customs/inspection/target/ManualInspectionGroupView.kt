package com.acme.transport.customs.inspection.target

/** How screens and exports show a manual inspection group. */
data class ManualInspectionGroupView(
    var code: String,
    var titleText: String,
    var pinned: String,
    var segment: String,
    var approvedText: String,
    var carrier: String,
    var odometer: String,
    var graceDays: String,
    var statusLabel: String,
)

fun toManualInspectionGroupView(item: ManualInspectionGroup): ManualInspectionGroupView {
    return ManualInspectionGroupView(
        code = item.code,
        titleText = item.title,
        pinned = "${item.pinned}",
        segment = item.segment,
        approvedText = "${item.approved}",
        carrier = item.carrier,
        odometer = item.odometer.toString(),
        graceDays = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualInspectionGroupViews(items: MutableList<ManualInspectionGroup>): MutableList<ManualInspectionGroupView> {
    val views = mutableListOf<ManualInspectionGroupView>()
    for (item in items) {
        views.add(toManualInspectionGroupView(item))
    }
    return views
}

fun sumManualInspectionGroupView(view: ManualInspectionGroupView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedOdometer.toDouble() + parsedGraceDays.toDouble()
}
