package com.acme.transport.customs.broker.slot

/** How screens and exports show a broker proposal. */
data class BrokerProposalView(
    var code: String,
    var title: String,
    var category: String,
    var endsAt: String,
    var contactEmail: String,
    var ratio: String,
    var currencyText: String,
    var palletsText: String,
    var statusLabel: String,
)

fun toBrokerProposalView(item: BrokerProposal): BrokerProposalView {
    return BrokerProposalView(
        code = item.code,
        title = item.title,
        category = item.category,
        endsAt = item.endsAt.toString(),
        contactEmail = item.contactEmail,
        ratio = "%.2f".format(item.ratio),
        currencyText = item.currency,
        palletsText = item.pallets.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBrokerProposalViews(items: MutableList<BrokerProposal>): MutableList<BrokerProposalView> {
    val views = mutableListOf<BrokerProposalView>()
    for (item in items) {
        views.add(toBrokerProposalView(item))
    }
    return views
}

fun sumBrokerProposalView(view: BrokerProposalView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedRatio = view.ratio.toDouble()
    val parsedPallets = view.palletsText.toInt()
    return parsedEndsAt.toDouble() + parsedRatio + parsedPallets.toDouble()
}
