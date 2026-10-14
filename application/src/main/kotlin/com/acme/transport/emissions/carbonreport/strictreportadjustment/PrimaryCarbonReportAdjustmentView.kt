package com.acme.transport.emissions.carbonreport.strictreportadjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary carbon report adjustment. */
data class PrimaryCarbonReportAdjustmentView(
    var code: String,
    var title: String,
    var expiresAt: String,
    var minimum: String,
    var categoryText: String,
    var updatedAtText: String,
    var statusLabel: String,
)

fun toPrimaryCarbonReportAdjustmentView(item: PrimaryCarbonReportAdjustment): PrimaryCarbonReportAdjustmentView {
    return PrimaryCarbonReportAdjustmentView(
        code = truncate(item.code, 80),
        title = item.title,
        expiresAt = item.expiresAt.toString(),
        minimum = "%.2f".format(item.minimum),
        categoryText = item.category,
        updatedAtText = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryCarbonReportAdjustmentViews(items: MutableList<PrimaryCarbonReportAdjustment>): MutableList<PrimaryCarbonReportAdjustmentView> {
    val views = mutableListOf<PrimaryCarbonReportAdjustmentView>()
    for (item in items) {
        views.add(toPrimaryCarbonReportAdjustmentView(item))
    }
    return views
}

fun sumPrimaryCarbonReportAdjustmentView(view: PrimaryCarbonReportAdjustmentView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedMinimum = view.minimum.toDouble()
    val parsedUpdatedAt = view.updatedAtText.toInt()
    return parsedExpiresAt.toDouble() + parsedMinimum + parsedUpdatedAt.toDouble()
}
