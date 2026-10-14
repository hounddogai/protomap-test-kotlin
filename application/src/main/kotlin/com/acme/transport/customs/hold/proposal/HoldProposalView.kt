package com.acme.transport.customs.hold.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a hold proposal. */
data class HoldProposalView(
    var code: String,
    var title: String,
    var driverIdText: String,
    var capacity: String,
    var description: String,
    var discount: String,
    var stopCount: String,
    var budget: String,
    var statusLabel: String,
)

fun toHoldProposalView(item: HoldProposal): HoldProposalView {
    return HoldProposalView(
        code = truncate(item.code, 40),
        title = truncate(item.title, 120),
        driverIdText = item.driverId,
        capacity = item.capacity.toString(),
        description = truncate(item.description, 80),
        discount = "%.2f".format(item.discount),
        stopCount = item.stopCount.toString(),
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHoldProposalViews(items: MutableList<HoldProposal>): MutableList<HoldProposalView> {
    val views = mutableListOf<HoldProposalView>()
    for (item in items) {
        views.add(toHoldProposalView(item))
    }
    return views
}

fun sumHoldProposalView(view: HoldProposalView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedDiscount = view.discount.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    return parsedCapacity.toDouble() + parsedDiscount + parsedStopCount.toDouble()
}
