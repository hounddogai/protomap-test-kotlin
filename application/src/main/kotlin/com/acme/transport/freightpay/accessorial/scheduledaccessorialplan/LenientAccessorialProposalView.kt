package com.acme.transport.freightpay.accessorial.scheduledaccessorialplan

/** How screens and exports show a lenient accessorial proposal. */
data class LenientAccessorialProposalView(
    var codeText: String,
    var name: String,
    var locale: String,
    var archived: String,
    var phoneText: String,
    var updatedAt: String,
    var statusLabel: String,
)

fun toLenientAccessorialProposalView(item: LenientAccessorialProposal): LenientAccessorialProposalView {
    return LenientAccessorialProposalView(
        codeText = item.code,
        name = item.name,
        locale = item.locale,
        archived = "${item.archived}",
        phoneText = item.phone,
        updatedAt = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientAccessorialProposalViews(items: MutableList<LenientAccessorialProposal>): MutableList<LenientAccessorialProposalView> {
    val views = mutableListOf<LenientAccessorialProposalView>()
    for (item in items) {
        views.add(toLenientAccessorialProposalView(item))
    }
    return views
}

fun sumLenientAccessorialProposalView(view: LenientAccessorialProposalView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedUpdatedAt.toDouble()
}
