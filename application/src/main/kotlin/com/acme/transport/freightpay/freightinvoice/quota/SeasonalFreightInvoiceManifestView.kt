package com.acme.transport.freightpay.freightinvoice.quota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal freight invoice manifest. */
data class SeasonalFreightInvoiceManifestView(
    var code: String,
    var label: String,
    var marginText: String,
    var limit: String,
    var floorValueText: String,
    var category: String,
    var threshold: String,
    var attempts: String,
    var countryText: String,
    var statusLabel: String,
)

fun toSeasonalFreightInvoiceManifestView(item: SeasonalFreightInvoiceManifest): SeasonalFreightInvoiceManifestView {
    return SeasonalFreightInvoiceManifestView(
        code = item.code,
        label = truncate(item.label, 80),
        marginText = "%.2f".format(item.margin),
        limit = item.limit.toString(),
        floorValueText = "%.2f".format(item.floorValue),
        category = item.category,
        threshold = "%.2f".format(item.threshold),
        attempts = item.attempts.toString(),
        countryText = truncate(item.country, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalFreightInvoiceManifestViews(items: MutableList<SeasonalFreightInvoiceManifest>): MutableList<SeasonalFreightInvoiceManifestView> {
    val views = mutableListOf<SeasonalFreightInvoiceManifestView>()
    for (item in items) {
        views.add(toSeasonalFreightInvoiceManifestView(item))
    }
    return views
}

fun sumSeasonalFreightInvoiceManifestView(view: SeasonalFreightInvoiceManifestView): Double {
    val parsedMargin = view.marginText.toDouble()
    val parsedLimit = view.limit.toInt()
    val parsedFloorValue = view.floorValueText.toDouble()
    return parsedMargin + parsedLimit.toDouble() + parsedFloorValue
}
