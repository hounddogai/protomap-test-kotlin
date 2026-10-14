package com.acme.transport.freightpay.dispute.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dispute profile. */
data class DisputeProfileView(
    var code: String,
    var title: String,
    var floorValueText: String,
    var locale: String,
    var ratioText: String,
    var palletsText: String,
    var ownerId: String,
    var statusLabel: String,
)

fun toDisputeProfileView(item: DisputeProfile): DisputeProfileView {
    return DisputeProfileView(
        code = truncate(item.code, 80),
        title = item.title,
        floorValueText = "%.2f".format(item.floorValue),
        locale = truncate(item.locale, 40),
        ratioText = "%.2f".format(item.ratio),
        palletsText = item.pallets.toString(),
        ownerId = truncate(item.ownerId, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDisputeProfileViews(items: MutableList<DisputeProfile>): MutableList<DisputeProfileView> {
    val views = mutableListOf<DisputeProfileView>()
    for (item in items) {
        views.add(toDisputeProfileView(item))
    }
    return views
}

fun sumDisputeProfileView(view: DisputeProfileView): Double {
    val parsedFloorValue = view.floorValueText.toDouble()
    val parsedRatio = view.ratioText.toDouble()
    val parsedPallets = view.palletsText.toInt()
    return parsedFloorValue + parsedRatio + parsedPallets.toDouble()
}
