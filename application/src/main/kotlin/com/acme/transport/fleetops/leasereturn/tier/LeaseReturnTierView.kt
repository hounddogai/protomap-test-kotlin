package com.acme.transport.fleetops.leasereturn.tier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lease return tier. */
data class LeaseReturnTierView(
    var code: String,
    var label: String,
    var lane: String,
    var archived: String,
    var segmentText: String,
    var amount: String,
    var quantity: String,
    var locale: String,
    var statusLabel: String,
)

fun toLeaseReturnTierView(item: LeaseReturnTier): LeaseReturnTierView {
    return LeaseReturnTierView(
        code = item.code,
        label = item.label,
        lane = item.lane,
        archived = "${item.archived}",
        segmentText = item.segment,
        amount = "%.2f".format(item.amount),
        quantity = item.quantity.toString(),
        locale = truncate(item.locale, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLeaseReturnTierViews(items: MutableList<LeaseReturnTier>): MutableList<LeaseReturnTierView> {
    val views = mutableListOf<LeaseReturnTierView>()
    for (item in items) {
        views.add(toLeaseReturnTierView(item))
    }
    return views
}

fun sumLeaseReturnTierView(view: LeaseReturnTierView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedQuantity = view.quantity.toInt()
    return parsedAmount + parsedQuantity.toDouble()
}
