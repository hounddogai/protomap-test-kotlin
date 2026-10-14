package com.acme.transport.linehaul.tender.window

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global tender proposal. */
data class GlobalTenderProposalView(
    var codeText: String,
    var nameText: String,
    var ratio: String,
    var capacity: String,
    var labelText: String,
    var sequence: String,
    var batchSizeText: String,
    var score: String,
    var cost: String,
    var notes: String,
    var statusLabel: String,
)

fun toGlobalTenderProposalView(item: GlobalTenderProposal): GlobalTenderProposalView {
    return GlobalTenderProposalView(
        codeText = item.code,
        nameText = item.name,
        ratio = "%.2f".format(item.ratio),
        capacity = item.capacity.toString(),
        labelText = item.label,
        sequence = item.sequence.toString(),
        batchSizeText = item.batchSize.toString(),
        score = "%.2f".format(item.score),
        cost = "%.2f".format(item.cost),
        notes = truncate(item.notes, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalTenderProposalViews(items: MutableList<GlobalTenderProposal>): MutableList<GlobalTenderProposalView> {
    val views = mutableListOf<GlobalTenderProposalView>()
    for (item in items) {
        views.add(toGlobalTenderProposalView(item))
    }
    return views
}

fun sumGlobalTenderProposalView(view: GlobalTenderProposalView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedCapacity = view.capacity.toInt()
    val parsedSequence = view.sequence.toInt()
    return parsedRatio + parsedCapacity.toDouble() + parsedSequence.toDouble()
}
