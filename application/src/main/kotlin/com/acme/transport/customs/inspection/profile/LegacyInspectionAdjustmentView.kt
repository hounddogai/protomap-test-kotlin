package com.acme.transport.customs.inspection.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy inspection adjustment. */
data class LegacyInspectionAdjustmentView(
    var codeText: String,
    var title: String,
    var version: String,
    var margin: String,
    var shiftCode: String,
    var limit: String,
    var rate: String,
    var statusLabel: String,
)

fun toLegacyInspectionAdjustmentView(item: LegacyInspectionAdjustment): LegacyInspectionAdjustmentView {
    return LegacyInspectionAdjustmentView(
        codeText = item.code,
        title = truncate(item.title, 64),
        version = item.version.toString(),
        margin = "%.2f".format(item.margin),
        shiftCode = item.shiftCode,
        limit = item.limit.toString(),
        rate = "%.2f".format(item.rate),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyInspectionAdjustmentViews(items: MutableList<LegacyInspectionAdjustment>): MutableList<LegacyInspectionAdjustmentView> {
    val views = mutableListOf<LegacyInspectionAdjustmentView>()
    for (item in items) {
        views.add(toLegacyInspectionAdjustmentView(item))
    }
    return views
}

fun sumLegacyInspectionAdjustmentView(view: LegacyInspectionAdjustmentView): Double {
    val parsedVersion = view.version.toInt()
    val parsedMargin = view.margin.toDouble()
    val parsedLimit = view.limit.toInt()
    return parsedVersion.toDouble() + parsedMargin + parsedLimit.toDouble()
}
