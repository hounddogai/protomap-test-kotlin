package com.acme.transport.freightpay.freightinvoice.baseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal freight invoice window. */
data class SeasonalFreightInvoiceWindowView(
    var codeText: String,
    var name: String,
    var budget: String,
    var batchSizeText: String,
    var limit: String,
    var endsAt: String,
    var statusLabel: String,
)

fun toSeasonalFreightInvoiceWindowView(item: SeasonalFreightInvoiceWindow): SeasonalFreightInvoiceWindowView {
    return SeasonalFreightInvoiceWindowView(
        codeText = item.code,
        name = truncate(item.name, 64),
        budget = "%.2f".format(item.budget),
        batchSizeText = item.batchSize.toString(),
        limit = item.limit.toString(),
        endsAt = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalFreightInvoiceWindowViews(items: MutableList<SeasonalFreightInvoiceWindow>): MutableList<SeasonalFreightInvoiceWindowView> {
    val views = mutableListOf<SeasonalFreightInvoiceWindowView>()
    for (item in items) {
        views.add(toSeasonalFreightInvoiceWindowView(item))
    }
    return views
}

fun sumSeasonalFreightInvoiceWindowView(view: SeasonalFreightInvoiceWindowView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedBatchSize = view.batchSizeText.toInt()
    val parsedLimit = view.limit.toInt()
    return parsedBudget + parsedBatchSize.toDouble() + parsedLimit.toDouble()
}
