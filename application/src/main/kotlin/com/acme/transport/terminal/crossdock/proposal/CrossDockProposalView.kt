package com.acme.transport.terminal.crossdock.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a cross dock proposal. */
data class CrossDockProposalView(
    var code: String,
    var name: String,
    var archived: String,
    var quantityText: String,
    var channelText: String,
    var score: String,
    var carrier: String,
    var locale: String,
    var statusLabel: String,
)

fun toCrossDockProposalView(item: CrossDockProposal): CrossDockProposalView {
    return CrossDockProposalView(
        code = truncate(item.code, 80),
        name = item.name,
        archived = "${item.archived}",
        quantityText = item.quantity.toString(),
        channelText = item.channel,
        score = "%.2f".format(item.score),
        carrier = item.carrier,
        locale = truncate(item.locale, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCrossDockProposalViews(items: MutableList<CrossDockProposal>): MutableList<CrossDockProposalView> {
    val views = mutableListOf<CrossDockProposalView>()
    for (item in items) {
        views.add(toCrossDockProposalView(item))
    }
    return views
}

fun sumCrossDockProposalView(view: CrossDockProposalView): Double {
    val parsedQuantity = view.quantityText.toInt()
    val parsedScore = view.score.toDouble()
    return parsedQuantity.toDouble() + parsedScore
}
