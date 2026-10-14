package com.acme.transport.linehaul.backhaul.request

/** How screens and exports show a adaptive backhaul proposal. */
data class AdaptiveBackhaulProposalView(
    var codeText: String,
    var title: String,
    var priority: String,
    var ratio: String,
    var weight: String,
    var statusLabel: String,
)

fun toAdaptiveBackhaulProposalView(item: AdaptiveBackhaulProposal): AdaptiveBackhaulProposalView {
    return AdaptiveBackhaulProposalView(
        codeText = item.code,
        title = item.title,
        priority = item.priority.toString(),
        ratio = "%.2f".format(item.ratio),
        weight = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveBackhaulProposalViews(items: MutableList<AdaptiveBackhaulProposal>): MutableList<AdaptiveBackhaulProposalView> {
    val views = mutableListOf<AdaptiveBackhaulProposalView>()
    for (item in items) {
        views.add(toAdaptiveBackhaulProposalView(item))
    }
    return views
}

fun sumAdaptiveBackhaulProposalView(view: AdaptiveBackhaulProposalView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedRatio = view.ratio.toDouble()
    val parsedWeight = view.weight.toDouble()
    return parsedPriority.toDouble() + parsedRatio + parsedWeight
}
