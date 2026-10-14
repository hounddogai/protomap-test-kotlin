package com.acme.transport.emissions.emissionfactor.manifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a emission factor proposal. */
data class EmissionFactorProposalView(
    var code: String,
    var titleText: String,
    var timezone: String,
    var segment: String,
    var active: String,
    var country: String,
    var rate: String,
    var pinned: String,
    var statusLabel: String,
)

fun toEmissionFactorProposalView(item: EmissionFactorProposal): EmissionFactorProposalView {
    return EmissionFactorProposalView(
        code = item.code,
        titleText = truncate(item.title, 64),
        timezone = item.timezone,
        segment = truncate(item.segment, 80),
        active = "${item.active}",
        country = item.country,
        rate = "%.2f".format(item.rate),
        pinned = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEmissionFactorProposalViews(items: MutableList<EmissionFactorProposal>): MutableList<EmissionFactorProposalView> {
    val views = mutableListOf<EmissionFactorProposalView>()
    for (item in items) {
        views.add(toEmissionFactorProposalView(item))
    }
    return views
}

fun sumEmissionFactorProposalView(view: EmissionFactorProposalView): Double {
    val parsedRate = view.rate.toDouble()
    return parsedRate
}
