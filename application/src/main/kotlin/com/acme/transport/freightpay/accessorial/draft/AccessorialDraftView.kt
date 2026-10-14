package com.acme.transport.freightpay.accessorial.draft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a accessorial draft. */
data class AccessorialDraftView(
    var codeText: String,
    var name: String,
    var timezone: String,
    var loadKg: String,
    var carrier: String,
    var checksum: String,
    var margin: String,
    var ceiling: String,
    var quantity: String,
    var reviewer: String,
    var statusLabel: String,
)

fun toAccessorialDraftView(item: AccessorialDraft): AccessorialDraftView {
    return AccessorialDraftView(
        codeText = item.code,
        name = item.name,
        timezone = item.timezone,
        loadKg = "%.2f".format(item.loadKg),
        carrier = truncate(item.carrier, 40),
        checksum = item.checksum,
        margin = "%.2f".format(item.margin),
        ceiling = "%.2f".format(item.ceiling),
        quantity = item.quantity.toString(),
        reviewer = item.reviewer,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAccessorialDraftViews(items: MutableList<AccessorialDraft>): MutableList<AccessorialDraftView> {
    val views = mutableListOf<AccessorialDraftView>()
    for (item in items) {
        views.add(toAccessorialDraftView(item))
    }
    return views
}

fun sumAccessorialDraftView(view: AccessorialDraftView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedMargin = view.margin.toDouble()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedLoadKg + parsedMargin + parsedCeiling
}
