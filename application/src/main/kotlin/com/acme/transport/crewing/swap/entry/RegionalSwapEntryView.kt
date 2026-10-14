package com.acme.transport.crewing.swap.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional swap entry. */
data class RegionalSwapEntryView(
    var codeText: String,
    var titleText: String,
    var amount: String,
    var externalRef: String,
    var unit: String,
    var pinned: String,
    var routeCode: String,
    var minimum: String,
    var segmentText: String,
    var region: String,
    var statusLabel: String,
)

fun toRegionalSwapEntryView(item: RegionalSwapEntry): RegionalSwapEntryView {
    return RegionalSwapEntryView(
        codeText = truncate(item.code, 120),
        titleText = truncate(item.title, 80),
        amount = "%.2f".format(item.amount),
        externalRef = item.externalRef,
        unit = truncate(item.unit, 80),
        pinned = "${item.pinned}",
        routeCode = item.routeCode,
        minimum = "%.2f".format(item.minimum),
        segmentText = item.segment,
        region = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalSwapEntryViews(items: MutableList<RegionalSwapEntry>): MutableList<RegionalSwapEntryView> {
    val views = mutableListOf<RegionalSwapEntryView>()
    for (item in items) {
        views.add(toRegionalSwapEntryView(item))
    }
    return views
}

fun sumRegionalSwapEntryView(view: RegionalSwapEntryView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedMinimum = view.minimum.toDouble()
    return parsedAmount + parsedMinimum
}
