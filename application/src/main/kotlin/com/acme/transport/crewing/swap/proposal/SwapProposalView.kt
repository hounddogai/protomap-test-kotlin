package com.acme.transport.crewing.swap.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a swap proposal. */
data class SwapProposalView(
    var codeText: String,
    var title: String,
    var category: String,
    var locale: String,
    var quantityText: String,
    var updatedAt: String,
    var retries: String,
    var notes: String,
    var margin: String,
    var odometer: String,
    var sequence: String,
    var statusLabel: String,
)

fun toSwapProposalView(item: SwapProposal): SwapProposalView {
    return SwapProposalView(
        codeText = item.code,
        title = truncate(item.title, 64),
        category = item.category,
        locale = item.locale,
        quantityText = item.quantity.toString(),
        updatedAt = item.updatedAt.toString(),
        retries = item.retries.toString(),
        notes = item.notes,
        margin = "%.2f".format(item.margin),
        odometer = item.odometer.toString(),
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSwapProposalViews(items: MutableList<SwapProposal>): MutableList<SwapProposalView> {
    val views = mutableListOf<SwapProposalView>()
    for (item in items) {
        views.add(toSwapProposalView(item))
    }
    return views
}

fun sumSwapProposalView(view: SwapProposalView): Double {
    val parsedQuantity = view.quantityText.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedRetries = view.retries.toInt()
    return parsedQuantity.toDouble() + parsedUpdatedAt.toDouble() + parsedRetries.toDouble()
}
