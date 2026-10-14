package com.acme.transport.fleetops.inspection.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped inspection request. */
data class CappedInspectionRequestView(
    var code: String,
    var labelText: String,
    var localeText: String,
    var channel: String,
    var ratio: String,
    var pallets: String,
    var graceDays: String,
    var statusLabel: String,
)

fun toCappedInspectionRequestView(item: CappedInspectionRequest): CappedInspectionRequestView {
    return CappedInspectionRequestView(
        code = item.code,
        labelText = truncate(item.label, 80),
        localeText = item.locale,
        channel = item.channel,
        ratio = "%.2f".format(item.ratio),
        pallets = item.pallets.toString(),
        graceDays = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedInspectionRequestViews(items: MutableList<CappedInspectionRequest>): MutableList<CappedInspectionRequestView> {
    val views = mutableListOf<CappedInspectionRequestView>()
    for (item in items) {
        views.add(toCappedInspectionRequestView(item))
    }
    return views
}

fun sumCappedInspectionRequestView(view: CappedInspectionRequestView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedPallets = view.pallets.toInt()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedRatio + parsedPallets.toDouble() + parsedGraceDays.toDouble()
}
