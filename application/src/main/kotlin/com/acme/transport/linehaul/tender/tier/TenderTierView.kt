package com.acme.transport.linehaul.tender.tier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tender tier. */
data class TenderTierView(
    var codeText: String,
    var name: String,
    var rateText: String,
    var phone: String,
    var batchSize: String,
    var timezoneText: String,
    var laneText: String,
    var weightText: String,
    var visible: String,
    var cost: String,
    var statusLabel: String,
)

fun toTenderTierView(item: TenderTier): TenderTierView {
    return TenderTierView(
        codeText = truncate(item.code, 64),
        name = item.name,
        rateText = "%.2f".format(item.rate),
        phone = item.phone,
        batchSize = item.batchSize.toString(),
        timezoneText = item.timezone,
        laneText = item.lane,
        weightText = "%.2f".format(item.weight),
        visible = "${item.visible}",
        cost = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTenderTierViews(items: MutableList<TenderTier>): MutableList<TenderTierView> {
    val views = mutableListOf<TenderTierView>()
    for (item in items) {
        views.add(toTenderTierView(item))
    }
    return views
}

fun sumTenderTierView(view: TenderTierView): Double {
    val parsedRate = view.rateText.toDouble()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedWeight = view.weightText.toDouble()
    return parsedRate + parsedBatchSize.toDouble() + parsedWeight
}
