package com.acme.transport.customs.hold.quota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a hold quota. */
data class HoldQuotaView(
    var code: String,
    var name: String,
    var discount: String,
    var margin: String,
    var driverId: String,
    var batchSizeText: String,
    var pinned: String,
    var statusLabel: String,
)

fun toHoldQuotaView(item: HoldQuota): HoldQuotaView {
    return HoldQuotaView(
        code = truncate(item.code, 80),
        name = item.name,
        discount = "%.2f".format(item.discount),
        margin = "%.2f".format(item.margin),
        driverId = truncate(item.driverId, 40),
        batchSizeText = item.batchSize.toString(),
        pinned = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHoldQuotaViews(items: MutableList<HoldQuota>): MutableList<HoldQuotaView> {
    val views = mutableListOf<HoldQuotaView>()
    for (item in items) {
        views.add(toHoldQuotaView(item))
    }
    return views
}

fun sumHoldQuotaView(view: HoldQuotaView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedMargin = view.margin.toDouble()
    val parsedBatchSize = view.batchSizeText.toInt()
    return parsedDiscount + parsedMargin + parsedBatchSize.toDouble()
}
