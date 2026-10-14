package com.acme.transport.customs.inspection.inspectionoverride

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive inspection draft. */
data class AdaptiveInspectionDraftView(
    var code: String,
    var name: String,
    var fuelLiters: String,
    var loadKg: String,
    var country: String,
    var depot: String,
    var statusLabel: String,
)

fun toAdaptiveInspectionDraftView(item: AdaptiveInspectionDraft): AdaptiveInspectionDraftView {
    return AdaptiveInspectionDraftView(
        code = item.code,
        name = item.name,
        fuelLiters = "%.2f".format(item.fuelLiters),
        loadKg = "%.2f".format(item.loadKg),
        country = item.country,
        depot = truncate(item.depot, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveInspectionDraftViews(items: MutableList<AdaptiveInspectionDraft>): MutableList<AdaptiveInspectionDraftView> {
    val views = mutableListOf<AdaptiveInspectionDraftView>()
    for (item in items) {
        views.add(toAdaptiveInspectionDraftView(item))
    }
    return views
}

fun sumAdaptiveInspectionDraftView(view: AdaptiveInspectionDraftView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedFuelLiters + parsedLoadKg
}
