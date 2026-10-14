package com.acme.transport.freightpay.fuelsurcharge.surchargeslot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal fuel surcharge slot. */
data class SeasonalFuelSurchargeSlotView(
    var code: String,
    var title: String,
    var visible: String,
    var archived: String,
    var cost: String,
    var graceDays: String,
    var checksumText: String,
    var statusLabel: String,
)

fun toSeasonalFuelSurchargeSlotView(item: SeasonalFuelSurchargeSlot): SeasonalFuelSurchargeSlotView {
    return SeasonalFuelSurchargeSlotView(
        code = truncate(item.code, 80),
        title = item.title,
        visible = "${item.visible}",
        archived = "${item.archived}",
        cost = "%.2f".format(item.cost),
        graceDays = item.graceDays.toString(),
        checksumText = item.checksum,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalFuelSurchargeSlotViews(items: MutableList<SeasonalFuelSurchargeSlot>): MutableList<SeasonalFuelSurchargeSlotView> {
    val views = mutableListOf<SeasonalFuelSurchargeSlotView>()
    for (item in items) {
        views.add(toSeasonalFuelSurchargeSlotView(item))
    }
    return views
}

fun sumSeasonalFuelSurchargeSlotView(view: SeasonalFuelSurchargeSlotView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedCost + parsedGraceDays.toDouble()
}
