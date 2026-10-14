package com.acme.transport.crewing.qualification.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional qualification proposal. */
data class RegionalQualificationProposalView(
    var code: String,
    var title: String,
    var quantityText: String,
    var palletsText: String,
    var floorValue: String,
    var retries: String,
    var statusLabel: String,
)

fun toRegionalQualificationProposalView(item: RegionalQualificationProposal): RegionalQualificationProposalView {
    return RegionalQualificationProposalView(
        code = item.code,
        title = truncate(item.title, 40),
        quantityText = item.quantity.toString(),
        palletsText = item.pallets.toString(),
        floorValue = "%.2f".format(item.floorValue),
        retries = item.retries.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalQualificationProposalViews(items: MutableList<RegionalQualificationProposal>): MutableList<RegionalQualificationProposalView> {
    val views = mutableListOf<RegionalQualificationProposalView>()
    for (item in items) {
        views.add(toRegionalQualificationProposalView(item))
    }
    return views
}

fun sumRegionalQualificationProposalView(view: RegionalQualificationProposalView): Double {
    val parsedQuantity = view.quantityText.toInt()
    val parsedPallets = view.palletsText.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedQuantity.toDouble() + parsedPallets.toDouble() + parsedFloorValue
}
