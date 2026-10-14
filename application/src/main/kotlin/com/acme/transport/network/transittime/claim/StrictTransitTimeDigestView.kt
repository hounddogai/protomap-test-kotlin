package com.acme.transport.network.transittime.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict transit time digest. */
data class StrictTransitTimeDigestView(
    var codeText: String,
    var titleText: String,
    var locale: String,
    var shiftCode: String,
    var stopCountText: String,
    var endsAt: String,
    var country: String,
    var carrier: String,
    var quantity: String,
    var fuelLiters: String,
    var ratio: String,
    var statusLabel: String,
)

fun toStrictTransitTimeDigestView(item: StrictTransitTimeDigest): StrictTransitTimeDigestView {
    return StrictTransitTimeDigestView(
        codeText = item.code,
        titleText = item.title,
        locale = truncate(item.locale, 40),
        shiftCode = item.shiftCode,
        stopCountText = item.stopCount.toString(),
        endsAt = item.endsAt.toString(),
        country = truncate(item.country, 80),
        carrier = item.carrier,
        quantity = item.quantity.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        ratio = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictTransitTimeDigestViews(items: MutableList<StrictTransitTimeDigest>): MutableList<StrictTransitTimeDigestView> {
    val views = mutableListOf<StrictTransitTimeDigestView>()
    for (item in items) {
        views.add(toStrictTransitTimeDigestView(item))
    }
    return views
}

fun sumStrictTransitTimeDigestView(view: StrictTransitTimeDigestView): Double {
    val parsedStopCount = view.stopCountText.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedQuantity = view.quantity.toInt()
    return parsedStopCount.toDouble() + parsedEndsAt.toDouble() + parsedQuantity.toDouble()
}
