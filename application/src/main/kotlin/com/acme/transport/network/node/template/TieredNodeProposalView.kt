package com.acme.transport.network.node.template

/** How screens and exports show a tiered node proposal. */
data class TieredNodeProposalView(
    var code: String,
    var labelText: String,
    var windowDaysText: String,
    var visible: String,
    var archived: String,
    var summary: String,
    var statusLabel: String,
)

fun toTieredNodeProposalView(item: TieredNodeProposal): TieredNodeProposalView {
    return TieredNodeProposalView(
        code = item.code,
        labelText = item.label,
        windowDaysText = item.windowDays.toString(),
        visible = "${item.visible}",
        archived = "${item.archived}",
        summary = item.summary,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredNodeProposalViews(items: MutableList<TieredNodeProposal>): MutableList<TieredNodeProposalView> {
    val views = mutableListOf<TieredNodeProposalView>()
    for (item in items) {
        views.add(toTieredNodeProposalView(item))
    }
    return views
}

fun sumTieredNodeProposalView(view: TieredNodeProposalView): Double {
    val parsedWindowDays = view.windowDaysText.toInt()
    return parsedWindowDays.toDouble()
}
