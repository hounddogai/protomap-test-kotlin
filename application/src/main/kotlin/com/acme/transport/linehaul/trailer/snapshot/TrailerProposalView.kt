package com.acme.transport.linehaul.trailer.snapshot

/** How screens and exports show a trailer proposal. */
data class TrailerProposalView(
    var codeText: String,
    var name: String,
    var phone: String,
    var segment: String,
    var minimumText: String,
    var currencyText: String,
    var active: String,
    var createdAt: String,
    var statusLabel: String,
)

fun toTrailerProposalView(item: TrailerProposal): TrailerProposalView {
    return TrailerProposalView(
        codeText = item.code,
        name = item.name,
        phone = item.phone,
        segment = item.segment,
        minimumText = "%.2f".format(item.minimum),
        currencyText = item.currency,
        active = "${item.active}",
        createdAt = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerProposalViews(items: MutableList<TrailerProposal>): MutableList<TrailerProposalView> {
    val views = mutableListOf<TrailerProposalView>()
    for (item in items) {
        views.add(toTrailerProposalView(item))
    }
    return views
}

fun sumTrailerProposalView(view: TrailerProposalView): Double {
    val parsedMinimum = view.minimumText.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedMinimum + parsedCreatedAt.toDouble()
}
