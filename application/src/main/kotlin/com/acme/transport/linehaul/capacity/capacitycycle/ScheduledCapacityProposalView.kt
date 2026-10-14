package com.acme.transport.linehaul.capacity.capacitycycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled capacity proposal. */
data class ScheduledCapacityProposalView(
    var codeText: String,
    var label: String,
    var source: String,
    var sequence: String,
    var shiftCodeText: String,
    var statusLabel: String,
)

fun toScheduledCapacityProposalView(item: ScheduledCapacityProposal): ScheduledCapacityProposalView {
    return ScheduledCapacityProposalView(
        codeText = item.code,
        label = truncate(item.label, 40),
        source = item.source,
        sequence = item.sequence.toString(),
        shiftCodeText = item.shiftCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledCapacityProposalViews(items: MutableList<ScheduledCapacityProposal>): MutableList<ScheduledCapacityProposalView> {
    val views = mutableListOf<ScheduledCapacityProposalView>()
    for (item in items) {
        views.add(toScheduledCapacityProposalView(item))
    }
    return views
}

fun sumScheduledCapacityProposalView(view: ScheduledCapacityProposalView): Double {
    val parsedSequence = view.sequence.toInt()
    return parsedSequence.toDouble()
}
