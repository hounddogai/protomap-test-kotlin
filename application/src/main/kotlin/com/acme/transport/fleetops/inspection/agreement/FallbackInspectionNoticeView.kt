package com.acme.transport.fleetops.inspection.agreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback inspection notice. */
data class FallbackInspectionNoticeView(
    var code: String,
    var name: String,
    var etaMinutesText: String,
    var approvedText: String,
    var category: String,
    var description: String,
    var cost: String,
    var loadKg: String,
    var notes: String,
    var batchSizeText: String,
    var statusLabel: String,
)

fun toFallbackInspectionNoticeView(item: FallbackInspectionNotice): FallbackInspectionNoticeView {
    return FallbackInspectionNoticeView(
        code = item.code,
        name = item.name,
        etaMinutesText = item.etaMinutes.toString(),
        approvedText = "${item.approved}",
        category = item.category,
        description = truncate(item.description, 80),
        cost = "%.2f".format(item.cost),
        loadKg = "%.2f".format(item.loadKg),
        notes = item.notes,
        batchSizeText = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackInspectionNoticeViews(items: MutableList<FallbackInspectionNotice>): MutableList<FallbackInspectionNoticeView> {
    val views = mutableListOf<FallbackInspectionNoticeView>()
    for (item in items) {
        views.add(toFallbackInspectionNoticeView(item))
    }
    return views
}

fun sumFallbackInspectionNoticeView(view: FallbackInspectionNoticeView): Double {
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedCost = view.cost.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedEtaMinutes.toDouble() + parsedCost + parsedLoadKg
}
