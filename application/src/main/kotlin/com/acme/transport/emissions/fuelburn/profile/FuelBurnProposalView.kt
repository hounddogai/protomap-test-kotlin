package com.acme.transport.emissions.fuelburn.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel burn proposal. */
data class FuelBurnProposalView(
    var code: String,
    var name: String,
    var vehicleId: String,
    var shiftCodeText: String,
    var rateText: String,
    var statusLabel: String,
)

fun toFuelBurnProposalView(item: FuelBurnProposal): FuelBurnProposalView {
    return FuelBurnProposalView(
        code = item.code,
        name = truncate(item.name, 64),
        vehicleId = truncate(item.vehicleId, 80),
        shiftCodeText = item.shiftCode,
        rateText = "%.2f".format(item.rate),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelBurnProposalViews(items: MutableList<FuelBurnProposal>): MutableList<FuelBurnProposalView> {
    val views = mutableListOf<FuelBurnProposalView>()
    for (item in items) {
        views.add(toFuelBurnProposalView(item))
    }
    return views
}

fun sumFuelBurnProposalView(view: FuelBurnProposalView): Double {
    val parsedRate = view.rateText.toDouble()
    return parsedRate
}
