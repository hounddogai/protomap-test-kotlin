package com.acme.transport.fleetops.tire.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tire proposal. */
data class TireProposalView(
    var code: String,
    var label: String,
    var driverIdText: String,
    var attempts: String,
    var cost: String,
    var quantityText: String,
    var titleText: String,
    var expiresAt: String,
    var statusLabel: String,
)

fun toTireProposalView(item: TireProposal): TireProposalView {
    return TireProposalView(
        code = truncate(item.code, 64),
        label = item.label,
        driverIdText = item.driverId,
        attempts = item.attempts.toString(),
        cost = "%.2f".format(item.cost),
        quantityText = item.quantity.toString(),
        titleText = item.title,
        expiresAt = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTireProposalViews(items: MutableList<TireProposal>): MutableList<TireProposalView> {
    val views = mutableListOf<TireProposalView>()
    for (item in items) {
        views.add(toTireProposalView(item))
    }
    return views
}

fun sumTireProposalView(view: TireProposalView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedCost = view.cost.toDouble()
    val parsedQuantity = view.quantityText.toInt()
    return parsedAttempts.toDouble() + parsedCost + parsedQuantity.toDouble()
}
