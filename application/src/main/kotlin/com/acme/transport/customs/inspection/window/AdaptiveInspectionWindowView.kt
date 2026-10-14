package com.acme.transport.customs.inspection.window

/** How screens and exports show a adaptive inspection window. */
data class AdaptiveInspectionWindowView(
    var code: String,
    var labelText: String,
    var ownerId: String,
    var etaMinutesText: String,
    var description: String,
    var checksumText: String,
    var depot: String,
    var statusLabel: String,
)

fun toAdaptiveInspectionWindowView(item: AdaptiveInspectionWindow): AdaptiveInspectionWindowView {
    return AdaptiveInspectionWindowView(
        code = item.code,
        labelText = item.label,
        ownerId = item.ownerId,
        etaMinutesText = item.etaMinutes.toString(),
        description = item.description,
        checksumText = item.checksum,
        depot = item.depot,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveInspectionWindowViews(items: MutableList<AdaptiveInspectionWindow>): MutableList<AdaptiveInspectionWindowView> {
    val views = mutableListOf<AdaptiveInspectionWindowView>()
    for (item in items) {
        views.add(toAdaptiveInspectionWindowView(item))
    }
    return views
}

fun sumAdaptiveInspectionWindowView(view: AdaptiveInspectionWindowView): Double {
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    return parsedEtaMinutes.toDouble()
}
