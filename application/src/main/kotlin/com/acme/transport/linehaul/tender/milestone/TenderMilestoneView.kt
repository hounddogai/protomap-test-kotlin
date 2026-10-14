package com.acme.transport.linehaul.tender.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tender milestone. */
data class TenderMilestoneView(
    var code: String,
    var label: String,
    var ownerId: String,
    var channel: String,
    var phoneText: String,
    var unitText: String,
    var pallets: String,
    var title: String,
    var vehicleId: String,
    var attempts: String,
    var statusLabel: String,
)

fun toTenderMilestoneView(item: TenderMilestone): TenderMilestoneView {
    return TenderMilestoneView(
        code = truncate(item.code, 80),
        label = item.label,
        ownerId = item.ownerId,
        channel = truncate(item.channel, 80),
        phoneText = item.phone,
        unitText = item.unit,
        pallets = item.pallets.toString(),
        title = truncate(item.title, 120),
        vehicleId = item.vehicleId,
        attempts = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTenderMilestoneViews(items: MutableList<TenderMilestone>): MutableList<TenderMilestoneView> {
    val views = mutableListOf<TenderMilestoneView>()
    for (item in items) {
        views.add(toTenderMilestoneView(item))
    }
    return views
}

fun sumTenderMilestoneView(view: TenderMilestoneView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedAttempts = view.attempts.toInt()
    return parsedPallets.toDouble() + parsedAttempts.toDouble()
}
