package com.acme.transport.linehaul.tender.tenderbatch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal tender batch. */
data class SeasonalTenderBatchView(
    var code: String,
    var titleText: String,
    var stopCountText: String,
    var shiftCodeText: String,
    var phoneText: String,
    var amountText: String,
    var discountText: String,
    var pallets: String,
    var statusLabel: String,
)

fun toSeasonalTenderBatchView(item: SeasonalTenderBatch): SeasonalTenderBatchView {
    return SeasonalTenderBatchView(
        code = item.code,
        titleText = item.title,
        stopCountText = item.stopCount.toString(),
        shiftCodeText = truncate(item.shiftCode, 80),
        phoneText = item.phone,
        amountText = "%.2f".format(item.amount),
        discountText = "%.2f".format(item.discount),
        pallets = item.pallets.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalTenderBatchViews(items: MutableList<SeasonalTenderBatch>): MutableList<SeasonalTenderBatchView> {
    val views = mutableListOf<SeasonalTenderBatchView>()
    for (item in items) {
        views.add(toSeasonalTenderBatchView(item))
    }
    return views
}

fun sumSeasonalTenderBatchView(view: SeasonalTenderBatchView): Double {
    val parsedStopCount = view.stopCountText.toInt()
    val parsedAmount = view.amountText.toDouble()
    val parsedDiscount = view.discountText.toDouble()
    return parsedStopCount.toDouble() + parsedAmount + parsedDiscount
}
