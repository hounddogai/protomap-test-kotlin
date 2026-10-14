package com.acme.transport.fleetops.inspection.profile

/** How screens and exports show a lenient inspection window. */
data class LenientInspectionWindowView(
    var code: String,
    var label: String,
    var regionText: String,
    var updatedAt: String,
    var archived: String,
    var statusLabel: String,
)

fun toLenientInspectionWindowView(item: LenientInspectionWindow): LenientInspectionWindowView {
    return LenientInspectionWindowView(
        code = item.code,
        label = item.label,
        regionText = item.region,
        updatedAt = item.updatedAt.toString(),
        archived = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientInspectionWindowViews(items: MutableList<LenientInspectionWindow>): MutableList<LenientInspectionWindowView> {
    val views = mutableListOf<LenientInspectionWindowView>()
    for (item in items) {
        views.add(toLenientInspectionWindowView(item))
    }
    return views
}

fun sumLenientInspectionWindowView(view: LenientInspectionWindowView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedUpdatedAt.toDouble()
}
