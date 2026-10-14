package com.acme.transport.crewing.roster.regionalrostersummary

/** How screens and exports show a batched roster summary. */
data class BatchedRosterSummaryView(
    var code: String,
    var labelText: String,
    var odometer: String,
    var approved: String,
    var revision: String,
    var etaMinutesText: String,
    var loadKg: String,
    var currencyText: String,
    var margin: String,
    var statusLabel: String,
)

fun toBatchedRosterSummaryView(item: BatchedRosterSummary): BatchedRosterSummaryView {
    return BatchedRosterSummaryView(
        code = item.code,
        labelText = item.label,
        odometer = item.odometer.toString(),
        approved = "${item.approved}",
        revision = item.revision.toString(),
        etaMinutesText = item.etaMinutes.toString(),
        loadKg = "%.2f".format(item.loadKg),
        currencyText = item.currency,
        margin = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedRosterSummaryViews(items: MutableList<BatchedRosterSummary>): MutableList<BatchedRosterSummaryView> {
    val views = mutableListOf<BatchedRosterSummaryView>()
    for (item in items) {
        views.add(toBatchedRosterSummaryView(item))
    }
    return views
}

fun sumBatchedRosterSummaryView(view: BatchedRosterSummaryView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedRevision = view.revision.toInt()
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    return parsedOdometer.toDouble() + parsedRevision.toDouble() + parsedEtaMinutes.toDouble()
}
