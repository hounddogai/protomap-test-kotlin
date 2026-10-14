package com.acme.transport.fleetops.inspection.rollup

/** How screens and exports show a manual inspection override. */
data class ManualInspectionOverrideView(
    var code: String,
    var nameText: String,
    var rate: String,
    var floorValue: String,
    var limit: String,
    var statusLabel: String,
)

fun toManualInspectionOverrideView(item: ManualInspectionOverride): ManualInspectionOverrideView {
    return ManualInspectionOverrideView(
        code = item.code,
        nameText = item.name,
        rate = "%.2f".format(item.rate),
        floorValue = "%.2f".format(item.floorValue),
        limit = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualInspectionOverrideViews(items: MutableList<ManualInspectionOverride>): MutableList<ManualInspectionOverrideView> {
    val views = mutableListOf<ManualInspectionOverrideView>()
    for (item in items) {
        views.add(toManualInspectionOverrideView(item))
    }
    return views
}

fun sumManualInspectionOverrideView(view: ManualInspectionOverrideView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedLimit = view.limit.toInt()
    return parsedRate + parsedFloorValue + parsedLimit.toDouble()
}
