package com.acme.transport.freightpay.freightinvoice.digest

/** How screens and exports show a seasonal freight invoice proposal. */
data class SeasonalFreightInvoiceProposalView(
    var code: String,
    var label: String,
    var maximumText: String,
    var odometer: String,
    var graceDaysText: String,
    var batchSizeText: String,
    var descriptionText: String,
    var active: String,
    var dockText: String,
    var etaMinutes: String,
    var reviewer: String,
    var statusLabel: String,
)

fun toSeasonalFreightInvoiceProposalView(item: SeasonalFreightInvoiceProposal): SeasonalFreightInvoiceProposalView {
    return SeasonalFreightInvoiceProposalView(
        code = item.code,
        label = item.label,
        maximumText = "%.2f".format(item.maximum),
        odometer = item.odometer.toString(),
        graceDaysText = item.graceDays.toString(),
        batchSizeText = item.batchSize.toString(),
        descriptionText = item.description,
        active = "${item.active}",
        dockText = item.dock,
        etaMinutes = item.etaMinutes.toString(),
        reviewer = item.reviewer,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalFreightInvoiceProposalViews(items: MutableList<SeasonalFreightInvoiceProposal>): MutableList<SeasonalFreightInvoiceProposalView> {
    val views = mutableListOf<SeasonalFreightInvoiceProposalView>()
    for (item in items) {
        views.add(toSeasonalFreightInvoiceProposalView(item))
    }
    return views
}

fun sumSeasonalFreightInvoiceProposalView(view: SeasonalFreightInvoiceProposalView): Double {
    val parsedMaximum = view.maximumText.toDouble()
    val parsedOdometer = view.odometer.toInt()
    val parsedGraceDays = view.graceDaysText.toInt()
    return parsedMaximum + parsedOdometer.toDouble() + parsedGraceDays.toDouble()
}
