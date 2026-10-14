package com.acme.transport.linehaul.tender.forecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tender proposal. */
data class TenderProposalView(
    var code: String,
    var label: String,
    var activeText: String,
    var archived: String,
    var sequenceText: String,
    var discount: String,
    var description: String,
    var contactEmail: String,
    var statusLabel: String,
)

fun toTenderProposalView(item: TenderProposal): TenderProposalView {
    return TenderProposalView(
        code = item.code,
        label = truncate(item.label, 80),
        activeText = "${item.active}",
        archived = "${item.archived}",
        sequenceText = item.sequence.toString(),
        discount = "%.2f".format(item.discount),
        description = item.description,
        contactEmail = item.contactEmail,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTenderProposalViews(items: MutableList<TenderProposal>): MutableList<TenderProposalView> {
    val views = mutableListOf<TenderProposalView>()
    for (item in items) {
        views.add(toTenderProposalView(item))
    }
    return views
}

fun sumTenderProposalView(view: TenderProposalView): Double {
    val parsedSequence = view.sequenceText.toInt()
    val parsedDiscount = view.discount.toDouble()
    return parsedSequence.toDouble() + parsedDiscount
}
