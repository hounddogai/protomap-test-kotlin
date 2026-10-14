package com.acme.transport.crewing.availability.availabilitytier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending availability tier. */
data class PendingAvailabilityTierView(
    var code: String,
    var labelText: String,
    var approved: String,
    var rateText: String,
    var ownerId: String,
    var pinned: String,
    var descriptionText: String,
    var regionText: String,
    var visible: String,
    var statusLabel: String,
)

fun toPendingAvailabilityTierView(item: PendingAvailabilityTier): PendingAvailabilityTierView {
    return PendingAvailabilityTierView(
        code = item.code,
        labelText = truncate(item.label, 120),
        approved = "${item.approved}",
        rateText = "%.2f".format(item.rate),
        ownerId = truncate(item.ownerId, 64),
        pinned = "${item.pinned}",
        descriptionText = item.description,
        regionText = truncate(item.region, 64),
        visible = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingAvailabilityTierViews(items: MutableList<PendingAvailabilityTier>): MutableList<PendingAvailabilityTierView> {
    val views = mutableListOf<PendingAvailabilityTierView>()
    for (item in items) {
        views.add(toPendingAvailabilityTierView(item))
    }
    return views
}

fun sumPendingAvailabilityTierView(view: PendingAvailabilityTierView): Double {
    val parsedRate = view.rateText.toDouble()
    return parsedRate
}
