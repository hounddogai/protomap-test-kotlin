package com.acme.transport.network.zone.mapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a zone slot. */
data class ZoneSlotView(
    var code: String,
    var titleText: String,
    var discount: String,
    var region: String,
    var sequence: String,
    var localeText: String,
    var active: String,
    var endsAtText: String,
    var quantity: String,
    var statusLabel: String,
)

fun toZoneSlotView(item: ZoneSlot): ZoneSlotView {
    return ZoneSlotView(
        code = item.code,
        titleText = truncate(item.title, 64),
        discount = "%.2f".format(item.discount),
        region = truncate(item.region, 120),
        sequence = item.sequence.toString(),
        localeText = item.locale,
        active = "${item.active}",
        endsAtText = item.endsAt.toString(),
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toZoneSlotViews(items: MutableList<ZoneSlot>): MutableList<ZoneSlotView> {
    val views = mutableListOf<ZoneSlotView>()
    for (item in items) {
        views.add(toZoneSlotView(item))
    }
    return views
}

fun sumZoneSlotView(view: ZoneSlotView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedSequence = view.sequence.toInt()
    val parsedEndsAt = view.endsAtText.toInt()
    return parsedDiscount + parsedSequence.toDouble() + parsedEndsAt.toDouble()
}
