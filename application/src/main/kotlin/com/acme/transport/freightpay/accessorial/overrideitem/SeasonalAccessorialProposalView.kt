package com.acme.transport.freightpay.accessorial.overrideitem

/** How screens and exports show a seasonal accessorial proposal. */
data class SeasonalAccessorialProposalView(
    var code: String,
    var label: String,
    var vehicleId: String,
    var timezone: String,
    var routeCode: String,
    var quantity: String,
    var statusLabel: String,
)

fun toSeasonalAccessorialProposalView(item: SeasonalAccessorialProposal): SeasonalAccessorialProposalView {
    return SeasonalAccessorialProposalView(
        code = item.code,
        label = item.label,
        vehicleId = item.vehicleId,
        timezone = item.timezone,
        routeCode = item.routeCode,
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalAccessorialProposalViews(items: MutableList<SeasonalAccessorialProposal>): MutableList<SeasonalAccessorialProposalView> {
    val views = mutableListOf<SeasonalAccessorialProposalView>()
    for (item in items) {
        views.add(toSeasonalAccessorialProposalView(item))
    }
    return views
}

fun sumSeasonalAccessorialProposalView(view: SeasonalAccessorialProposalView): Double {
    val parsedQuantity = view.quantity.toInt()
    return parsedQuantity.toDouble()
}
