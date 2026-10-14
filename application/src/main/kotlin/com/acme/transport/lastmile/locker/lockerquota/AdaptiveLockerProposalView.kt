package com.acme.transport.lastmile.locker.lockerquota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive locker proposal. */
data class AdaptiveLockerProposalView(
    var codeText: String,
    var name: String,
    var capacityText: String,
    var currency: String,
    var updatedAt: String,
    var ceiling: String,
    var budget: String,
    var phoneText: String,
    var statusLabel: String,
)

fun toAdaptiveLockerProposalView(item: AdaptiveLockerProposal): AdaptiveLockerProposalView {
    return AdaptiveLockerProposalView(
        codeText = truncate(item.code, 64),
        name = item.name,
        capacityText = item.capacity.toString(),
        currency = item.currency,
        updatedAt = item.updatedAt.toString(),
        ceiling = "%.2f".format(item.ceiling),
        budget = "%.2f".format(item.budget),
        phoneText = item.phone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveLockerProposalViews(items: MutableList<AdaptiveLockerProposal>): MutableList<AdaptiveLockerProposalView> {
    val views = mutableListOf<AdaptiveLockerProposalView>()
    for (item in items) {
        views.add(toAdaptiveLockerProposalView(item))
    }
    return views
}

fun sumAdaptiveLockerProposalView(view: AdaptiveLockerProposalView): Double {
    val parsedCapacity = view.capacityText.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedCapacity.toDouble() + parsedUpdatedAt.toDouble() + parsedCeiling
}
