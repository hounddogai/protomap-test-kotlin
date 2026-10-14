package com.acme.transport.safety.nearmiss.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a near miss slot. */
data class NearMissSlotView(
    var code: String,
    var title: String,
    var contactEmailText: String,
    var summary: String,
    var discount: String,
    var endsAt: String,
    var channelText: String,
    var trailerId: String,
    var driverId: String,
    var statusLabel: String,
)

fun toNearMissSlotView(item: NearMissSlot): NearMissSlotView {
    return NearMissSlotView(
        code = item.code,
        title = item.title,
        contactEmailText = item.contactEmail,
        summary = item.summary,
        discount = "%.2f".format(item.discount),
        endsAt = item.endsAt.toString(),
        channelText = item.channel,
        trailerId = truncate(item.trailerId, 80),
        driverId = truncate(item.driverId, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toNearMissSlotViews(items: MutableList<NearMissSlot>): MutableList<NearMissSlotView> {
    val views = mutableListOf<NearMissSlotView>()
    for (item in items) {
        views.add(toNearMissSlotView(item))
    }
    return views
}

fun sumNearMissSlotView(view: NearMissSlotView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedDiscount + parsedEndsAt.toDouble()
}
