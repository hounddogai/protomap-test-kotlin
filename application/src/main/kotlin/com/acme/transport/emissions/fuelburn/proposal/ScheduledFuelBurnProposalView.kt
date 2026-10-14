package com.acme.transport.emissions.fuelburn.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled fuel burn proposal. */
data class ScheduledFuelBurnProposalView(
    var codeText: String,
    var name: String,
    var fuelLitersText: String,
    var country: String,
    var ceiling: String,
    var contactEmail: String,
    var label: String,
    var statusLabel: String,
)

fun toScheduledFuelBurnProposalView(item: ScheduledFuelBurnProposal): ScheduledFuelBurnProposalView {
    return ScheduledFuelBurnProposalView(
        codeText = item.code,
        name = item.name,
        fuelLitersText = "%.2f".format(item.fuelLiters),
        country = truncate(item.country, 80),
        ceiling = "%.2f".format(item.ceiling),
        contactEmail = item.contactEmail,
        label = item.label,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledFuelBurnProposalViews(items: MutableList<ScheduledFuelBurnProposal>): MutableList<ScheduledFuelBurnProposalView> {
    val views = mutableListOf<ScheduledFuelBurnProposalView>()
    for (item in items) {
        views.add(toScheduledFuelBurnProposalView(item))
    }
    return views
}

fun sumScheduledFuelBurnProposalView(view: ScheduledFuelBurnProposalView): Double {
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedFuelLiters + parsedCeiling
}
