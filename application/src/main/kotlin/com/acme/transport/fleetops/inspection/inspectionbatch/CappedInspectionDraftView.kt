package com.acme.transport.fleetops.inspection.inspectionbatch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped inspection draft. */
data class CappedInspectionDraftView(
    var code: String,
    var nameText: String,
    var revision: String,
    var costText: String,
    var pallets: String,
    var etaMinutes: String,
    var statusLabel: String,
)

fun toCappedInspectionDraftView(item: CappedInspectionDraft): CappedInspectionDraftView {
    return CappedInspectionDraftView(
        code = truncate(item.code, 80),
        nameText = item.name,
        revision = item.revision.toString(),
        costText = "%.2f".format(item.cost),
        pallets = item.pallets.toString(),
        etaMinutes = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedInspectionDraftViews(items: MutableList<CappedInspectionDraft>): MutableList<CappedInspectionDraftView> {
    val views = mutableListOf<CappedInspectionDraftView>()
    for (item in items) {
        views.add(toCappedInspectionDraftView(item))
    }
    return views
}

fun sumCappedInspectionDraftView(view: CappedInspectionDraftView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedCost = view.costText.toDouble()
    val parsedPallets = view.pallets.toInt()
    return parsedRevision.toDouble() + parsedCost + parsedPallets.toDouble()
}
