package com.acme.transport.lastmile.proofofdelivery.deliverymanifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped proof of delivery allocation. */
data class CappedProofOfDeliveryAllocationView(
    var code: String,
    var title: String,
    var channel: String,
    var startsAt: String,
    var windowDaysText: String,
    var statusLabel: String,
)

fun toCappedProofOfDeliveryAllocationView(item: CappedProofOfDeliveryAllocation): CappedProofOfDeliveryAllocationView {
    return CappedProofOfDeliveryAllocationView(
        code = truncate(item.code, 64),
        title = item.title,
        channel = item.channel,
        startsAt = item.startsAt.toString(),
        windowDaysText = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedProofOfDeliveryAllocationViews(items: MutableList<CappedProofOfDeliveryAllocation>): MutableList<CappedProofOfDeliveryAllocationView> {
    val views = mutableListOf<CappedProofOfDeliveryAllocationView>()
    for (item in items) {
        views.add(toCappedProofOfDeliveryAllocationView(item))
    }
    return views
}

fun sumCappedProofOfDeliveryAllocationView(view: CappedProofOfDeliveryAllocationView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedWindowDays = view.windowDaysText.toInt()
    return parsedStartsAt.toDouble() + parsedWindowDays.toDouble()
}
