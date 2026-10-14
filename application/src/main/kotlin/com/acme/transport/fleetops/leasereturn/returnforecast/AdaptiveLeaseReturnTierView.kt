package com.acme.transport.fleetops.leasereturn.returnforecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive lease return tier. */
data class AdaptiveLeaseReturnTierView(
    var code: String,
    var title: String,
    var cost: String,
    var reviewer: String,
    var attempts: String,
    var graceDays: String,
    var label: String,
    var archived: String,
    var statusLabel: String,
)

fun toAdaptiveLeaseReturnTierView(item: AdaptiveLeaseReturnTier): AdaptiveLeaseReturnTierView {
    return AdaptiveLeaseReturnTierView(
        code = item.code,
        title = truncate(item.title, 40),
        cost = "%.2f".format(item.cost),
        reviewer = item.reviewer,
        attempts = item.attempts.toString(),
        graceDays = item.graceDays.toString(),
        label = truncate(item.label, 40),
        archived = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveLeaseReturnTierViews(items: MutableList<AdaptiveLeaseReturnTier>): MutableList<AdaptiveLeaseReturnTierView> {
    val views = mutableListOf<AdaptiveLeaseReturnTierView>()
    for (item in items) {
        views.add(toAdaptiveLeaseReturnTierView(item))
    }
    return views
}

fun sumAdaptiveLeaseReturnTierView(view: AdaptiveLeaseReturnTierView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedAttempts = view.attempts.toInt()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedCost + parsedAttempts.toDouble() + parsedGraceDays.toDouble()
}
