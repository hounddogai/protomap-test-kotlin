package com.acme.transport.linehaul.tender.batch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tender claim. */
data class TenderClaimView(
    var code: String,
    var name: String,
    var ratio: String,
    var capacity: String,
    var laneText: String,
    var maximum: String,
    var statusLabel: String,
)

fun toTenderClaimView(item: TenderClaim): TenderClaimView {
    return TenderClaimView(
        code = truncate(item.code, 40),
        name = truncate(item.name, 80),
        ratio = "%.2f".format(item.ratio),
        capacity = item.capacity.toString(),
        laneText = item.lane,
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTenderClaimViews(items: MutableList<TenderClaim>): MutableList<TenderClaimView> {
    val views = mutableListOf<TenderClaimView>()
    for (item in items) {
        views.add(toTenderClaimView(item))
    }
    return views
}

fun sumTenderClaimView(view: TenderClaimView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedCapacity = view.capacity.toInt()
    val parsedMaximum = view.maximum.toDouble()
    return parsedRatio + parsedCapacity.toDouble() + parsedMaximum
}
