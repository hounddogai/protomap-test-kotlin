package com.acme.transport.freightpay.freightinvoice.profile

/** How screens and exports show a regional freight invoice summary. */
data class RegionalFreightInvoiceSummaryView(
    var codeText: String,
    var title: String,
    var shiftCode: String,
    var windowDays: String,
    var distanceKm: String,
    var threshold: String,
    var version: String,
    var sourceText: String,
    var durationMinutes: String,
    var statusLabel: String,
)

fun toRegionalFreightInvoiceSummaryView(item: RegionalFreightInvoiceSummary): RegionalFreightInvoiceSummaryView {
    return RegionalFreightInvoiceSummaryView(
        codeText = item.code,
        title = item.title,
        shiftCode = item.shiftCode,
        windowDays = item.windowDays.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        threshold = "%.2f".format(item.threshold),
        version = item.version.toString(),
        sourceText = item.source,
        durationMinutes = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalFreightInvoiceSummaryViews(items: MutableList<RegionalFreightInvoiceSummary>): MutableList<RegionalFreightInvoiceSummaryView> {
    val views = mutableListOf<RegionalFreightInvoiceSummaryView>()
    for (item in items) {
        views.add(toRegionalFreightInvoiceSummaryView(item))
    }
    return views
}

fun sumRegionalFreightInvoiceSummaryView(view: RegionalFreightInvoiceSummaryView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    return parsedWindowDays.toDouble() + parsedDistanceKm + parsedThreshold
}
