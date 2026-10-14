package com.acme.transport.freightpay.freightinvoice.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a freight invoice rollup. */
data class FreightInvoiceRollupView(
    var code: String,
    var title: String,
    var ratio: String,
    var score: String,
    var source: String,
    var statusLabel: String,
)

fun toFreightInvoiceRollupView(item: FreightInvoiceRollup): FreightInvoiceRollupView {
    return FreightInvoiceRollupView(
        code = item.code,
        title = item.title,
        ratio = "%.2f".format(item.ratio),
        score = "%.2f".format(item.score),
        source = truncate(item.source, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFreightInvoiceRollupViews(items: MutableList<FreightInvoiceRollup>): MutableList<FreightInvoiceRollupView> {
    val views = mutableListOf<FreightInvoiceRollupView>()
    for (item in items) {
        views.add(toFreightInvoiceRollupView(item))
    }
    return views
}

fun sumFreightInvoiceRollupView(view: FreightInvoiceRollupView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedScore = view.score.toDouble()
    return parsedRatio + parsedScore
}
