package com.acme.transport.fleetops.leasereturn.returnledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary lease return proposal. */
data class PrimaryLeaseReturnProposalView(
    var code: String,
    var title: String,
    var sequenceText: String,
    var segment: String,
    var carrier: String,
    var externalRefText: String,
    var shiftCode: String,
    var statusLabel: String,
)

fun toPrimaryLeaseReturnProposalView(item: PrimaryLeaseReturnProposal): PrimaryLeaseReturnProposalView {
    return PrimaryLeaseReturnProposalView(
        code = truncate(item.code, 64),
        title = item.title,
        sequenceText = item.sequence.toString(),
        segment = item.segment,
        carrier = item.carrier,
        externalRefText = truncate(item.externalRef, 40),
        shiftCode = item.shiftCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryLeaseReturnProposalViews(items: MutableList<PrimaryLeaseReturnProposal>): MutableList<PrimaryLeaseReturnProposalView> {
    val views = mutableListOf<PrimaryLeaseReturnProposalView>()
    for (item in items) {
        views.add(toPrimaryLeaseReturnProposalView(item))
    }
    return views
}

fun sumPrimaryLeaseReturnProposalView(view: PrimaryLeaseReturnProposalView): Double {
    val parsedSequence = view.sequenceText.toInt()
    return parsedSequence.toDouble()
}
