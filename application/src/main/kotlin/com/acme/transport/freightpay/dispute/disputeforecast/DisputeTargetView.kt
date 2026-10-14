package com.acme.transport.freightpay.dispute.disputeforecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dispute target. */
data class DisputeTargetView(
    var code: String,
    var title: String,
    var discountText: String,
    var currency: String,
    var reviewer: String,
    var sequence: String,
    var endsAt: String,
    var revision: String,
    var pallets: String,
    var distanceKmText: String,
    var statusLabel: String,
)

fun toDisputeTargetView(item: DisputeTarget): DisputeTargetView {
    return DisputeTargetView(
        code = truncate(item.code, 80),
        title = truncate(item.title, 64),
        discountText = "%.2f".format(item.discount),
        currency = item.currency,
        reviewer = item.reviewer,
        sequence = item.sequence.toString(),
        endsAt = item.endsAt.toString(),
        revision = item.revision.toString(),
        pallets = item.pallets.toString(),
        distanceKmText = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDisputeTargetViews(items: MutableList<DisputeTarget>): MutableList<DisputeTargetView> {
    val views = mutableListOf<DisputeTargetView>()
    for (item in items) {
        views.add(toDisputeTargetView(item))
    }
    return views
}

fun sumDisputeTargetView(view: DisputeTargetView): Double {
    val parsedDiscount = view.discountText.toDouble()
    val parsedSequence = view.sequence.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedDiscount + parsedSequence.toDouble() + parsedEndsAt.toDouble()
}
