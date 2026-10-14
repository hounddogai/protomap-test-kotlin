package com.acme.transport.freightpay.fuelsurcharge.audit

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal fuel surcharge tier. */
data class SeasonalFuelSurchargeTierView(
    var codeText: String,
    var label: String,
    var ratio: String,
    var etaMinutes: String,
    var active: String,
    var floorValue: String,
    var statusLabel: String,
)

fun toSeasonalFuelSurchargeTierView(item: SeasonalFuelSurchargeTier): SeasonalFuelSurchargeTierView {
    return SeasonalFuelSurchargeTierView(
        codeText = truncate(item.code, 80),
        label = item.label,
        ratio = "%.2f".format(item.ratio),
        etaMinutes = item.etaMinutes.toString(),
        active = "${item.active}",
        floorValue = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalFuelSurchargeTierViews(items: MutableList<SeasonalFuelSurchargeTier>): MutableList<SeasonalFuelSurchargeTierView> {
    val views = mutableListOf<SeasonalFuelSurchargeTierView>()
    for (item in items) {
        views.add(toSeasonalFuelSurchargeTierView(item))
    }
    return views
}

fun sumSeasonalFuelSurchargeTierView(view: SeasonalFuelSurchargeTierView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedRatio + parsedEtaMinutes.toDouble() + parsedFloorValue
}
