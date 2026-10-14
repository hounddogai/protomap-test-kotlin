package com.acme.transport.fleetops.inspection.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a inspection proposal. */
data class InspectionProposalView(
    var code: String,
    var titleText: String,
    var routeCode: String,
    var summaryText: String,
    var channel: String,
    var quantityText: String,
    var statusLabel: String,
)

fun toInspectionProposalView(item: InspectionProposal): InspectionProposalView {
    return InspectionProposalView(
        code = item.code,
        titleText = item.title,
        routeCode = truncate(item.routeCode, 40),
        summaryText = item.summary,
        channel = item.channel,
        quantityText = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionProposalViews(items: MutableList<InspectionProposal>): MutableList<InspectionProposalView> {
    val views = mutableListOf<InspectionProposalView>()
    for (item in items) {
        views.add(toInspectionProposalView(item))
    }
    return views
}

fun sumInspectionProposalView(view: InspectionProposalView): Double {
    val parsedQuantity = view.quantityText.toInt()
    return parsedQuantity.toDouble()
}
