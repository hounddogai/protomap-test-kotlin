package com.acme.transport.linehaul.tender.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tender allocation. */
data class TenderAllocationView(
    var codeText: String,
    var labelText: String,
    var category: String,
    var retries: String,
    var rate: String,
    var region: String,
    var channel: String,
    var weight: String,
    var statusLabel: String,
)

fun toTenderAllocationView(item: TenderAllocation): TenderAllocationView {
    return TenderAllocationView(
        codeText = item.code,
        labelText = item.label,
        category = item.category,
        retries = item.retries.toString(),
        rate = "%.2f".format(item.rate),
        region = item.region,
        channel = truncate(item.channel, 80),
        weight = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTenderAllocationViews(items: MutableList<TenderAllocation>): MutableList<TenderAllocationView> {
    val views = mutableListOf<TenderAllocationView>()
    for (item in items) {
        views.add(toTenderAllocationView(item))
    }
    return views
}

fun sumTenderAllocationView(view: TenderAllocationView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedRate = view.rate.toDouble()
    val parsedWeight = view.weight.toDouble()
    return parsedRetries.toDouble() + parsedRate + parsedWeight
}
