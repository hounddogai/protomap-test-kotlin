package com.acme.transport.customs.declaration.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a declaration proposal. */
data class DeclarationProposalView(
    var code: String,
    var name: String,
    var vehicleIdText: String,
    var distanceKmText: String,
    var palletsText: String,
    var statusLabel: String,
)

fun toDeclarationProposalView(item: DeclarationProposal): DeclarationProposalView {
    return DeclarationProposalView(
        code = truncate(item.code, 40),
        name = item.name,
        vehicleIdText = item.vehicleId,
        distanceKmText = "%.2f".format(item.distanceKm),
        palletsText = item.pallets.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDeclarationProposalViews(items: MutableList<DeclarationProposal>): MutableList<DeclarationProposalView> {
    val views = mutableListOf<DeclarationProposalView>()
    for (item in items) {
        views.add(toDeclarationProposalView(item))
    }
    return views
}

fun sumDeclarationProposalView(view: DeclarationProposalView): Double {
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedPallets = view.palletsText.toInt()
    return parsedDistanceKm + parsedPallets.toDouble()
}
