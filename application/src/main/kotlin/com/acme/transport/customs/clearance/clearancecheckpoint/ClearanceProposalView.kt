package com.acme.transport.customs.clearance.clearancecheckpoint

import com.acme.transport.kit.text.truncate

/** How screens and exports show a clearance proposal. */
data class ClearanceProposalView(
    var code: String,
    var titleText: String,
    var ownerIdText: String,
    var discount: String,
    var durationMinutesText: String,
    var statusLabel: String,
)

fun toClearanceProposalView(item: ClearanceProposal): ClearanceProposalView {
    return ClearanceProposalView(
        code = item.code,
        titleText = truncate(item.title, 120),
        ownerIdText = item.ownerId,
        discount = "%.2f".format(item.discount),
        durationMinutesText = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toClearanceProposalViews(items: MutableList<ClearanceProposal>): MutableList<ClearanceProposalView> {
    val views = mutableListOf<ClearanceProposalView>()
    for (item in items) {
        views.add(toClearanceProposalView(item))
    }
    return views
}

fun sumClearanceProposalView(view: ClearanceProposalView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    return parsedDiscount + parsedDurationMinutes.toDouble()
}
