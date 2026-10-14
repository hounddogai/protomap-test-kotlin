package com.acme.transport.freightpay.ratecard.adjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual rate card adjustment. */
data class ManualRateCardAdjustmentView(
    var code: String,
    var titleText: String,
    var reviewer: String,
    var discount: String,
    var vehicleId: String,
    var statusLabel: String,
)

fun toManualRateCardAdjustmentView(item: ManualRateCardAdjustment): ManualRateCardAdjustmentView {
    return ManualRateCardAdjustmentView(
        code = item.code,
        titleText = item.title,
        reviewer = item.reviewer,
        discount = "%.2f".format(item.discount),
        vehicleId = truncate(item.vehicleId, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualRateCardAdjustmentViews(items: MutableList<ManualRateCardAdjustment>): MutableList<ManualRateCardAdjustmentView> {
    val views = mutableListOf<ManualRateCardAdjustmentView>()
    for (item in items) {
        views.add(toManualRateCardAdjustmentView(item))
    }
    return views
}

fun sumManualRateCardAdjustmentView(view: ManualRateCardAdjustmentView): Double {
    val parsedDiscount = view.discount.toDouble()
    return parsedDiscount
}
