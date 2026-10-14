package com.acme.transport.network.zone.digest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a zone digest. */
data class ZoneDigestView(
    var code: String,
    var titleText: String,
    var amount: String,
    var quantity: String,
    var approvedText: String,
    var country: String,
    var archived: String,
    var endsAt: String,
    var maximum: String,
    var statusLabel: String,
)

fun toZoneDigestView(item: ZoneDigest): ZoneDigestView {
    return ZoneDigestView(
        code = item.code,
        titleText = truncate(item.title, 120),
        amount = "%.2f".format(item.amount),
        quantity = item.quantity.toString(),
        approvedText = "${item.approved}",
        country = item.country,
        archived = "${item.archived}",
        endsAt = item.endsAt.toString(),
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toZoneDigestViews(items: MutableList<ZoneDigest>): MutableList<ZoneDigestView> {
    val views = mutableListOf<ZoneDigestView>()
    for (item in items) {
        views.add(toZoneDigestView(item))
    }
    return views
}

fun sumZoneDigestView(view: ZoneDigestView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedQuantity = view.quantity.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedAmount + parsedQuantity.toDouble() + parsedEndsAt.toDouble()
}
