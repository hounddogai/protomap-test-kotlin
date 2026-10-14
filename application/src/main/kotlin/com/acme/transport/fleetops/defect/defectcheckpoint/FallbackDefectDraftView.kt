package com.acme.transport.fleetops.defect.defectcheckpoint

/** How screens and exports show a fallback defect draft. */
data class FallbackDefectDraftView(
    var code: String,
    var label: String,
    var locale: String,
    var threshold: String,
    var fuelLiters: String,
    var statusLabel: String,
)

fun toFallbackDefectDraftView(item: FallbackDefectDraft): FallbackDefectDraftView {
    return FallbackDefectDraftView(
        code = item.code,
        label = item.label,
        locale = item.locale,
        threshold = "%.2f".format(item.threshold),
        fuelLiters = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackDefectDraftViews(items: MutableList<FallbackDefectDraft>): MutableList<FallbackDefectDraftView> {
    val views = mutableListOf<FallbackDefectDraftView>()
    for (item in items) {
        views.add(toFallbackDefectDraftView(item))
    }
    return views
}

fun sumFallbackDefectDraftView(view: FallbackDefectDraftView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedThreshold + parsedFuelLiters
}
