package com.acme.transport.emissions.evcharge.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered ev charge proposal. */
data class TieredEvChargeProposalView(
    var code: String,
    var labelText: String,
    var ownerIdText: String,
    var unit: String,
    var channel: String,
    var quantityText: String,
    var statusLabel: String,
)

fun toTieredEvChargeProposalView(item: TieredEvChargeProposal): TieredEvChargeProposalView {
    return TieredEvChargeProposalView(
        code = truncate(item.code, 120),
        labelText = item.label,
        ownerIdText = item.ownerId,
        unit = truncate(item.unit, 40),
        channel = item.channel,
        quantityText = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredEvChargeProposalViews(items: MutableList<TieredEvChargeProposal>): MutableList<TieredEvChargeProposalView> {
    val views = mutableListOf<TieredEvChargeProposalView>()
    for (item in items) {
        views.add(toTieredEvChargeProposalView(item))
    }
    return views
}

fun sumTieredEvChargeProposalView(view: TieredEvChargeProposalView): Double {
    val parsedQuantity = view.quantityText.toInt()
    return parsedQuantity.toDouble()
}
