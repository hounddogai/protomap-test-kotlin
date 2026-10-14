package com.acme.transport.freightpay.fuelsurcharge.baseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel surcharge baseline. */
data class FuelSurchargeBaselineView(
    var codeText: String,
    var title: String,
    var etaMinutesText: String,
    var margin: String,
    var label: String,
    var loadKgText: String,
    var shiftCode: String,
    var durationMinutes: String,
    var stopCountText: String,
    var statusLabel: String,
)

fun toFuelSurchargeBaselineView(item: FuelSurchargeBaseline): FuelSurchargeBaselineView {
    return FuelSurchargeBaselineView(
        codeText = truncate(item.code, 64),
        title = truncate(item.title, 40),
        etaMinutesText = item.etaMinutes.toString(),
        margin = "%.2f".format(item.margin),
        label = item.label,
        loadKgText = "%.2f".format(item.loadKg),
        shiftCode = truncate(item.shiftCode, 64),
        durationMinutes = item.durationMinutes.toString(),
        stopCountText = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelSurchargeBaselineViews(items: MutableList<FuelSurchargeBaseline>): MutableList<FuelSurchargeBaselineView> {
    val views = mutableListOf<FuelSurchargeBaselineView>()
    for (item in items) {
        views.add(toFuelSurchargeBaselineView(item))
    }
    return views
}

fun sumFuelSurchargeBaselineView(view: FuelSurchargeBaselineView): Double {
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedMargin = view.margin.toDouble()
    val parsedLoadKg = view.loadKgText.toDouble()
    return parsedEtaMinutes.toDouble() + parsedMargin + parsedLoadKg
}
