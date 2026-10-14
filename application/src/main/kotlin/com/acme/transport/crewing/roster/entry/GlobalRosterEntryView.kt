package com.acme.transport.crewing.roster.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global roster entry. */
data class GlobalRosterEntryView(
    var codeText: String,
    var nameText: String,
    var discount: String,
    var floorValue: String,
    var weight: String,
    var trailerIdText: String,
    var currency: String,
    var attempts: String,
    var externalRefText: String,
    var statusLabel: String,
)

fun toGlobalRosterEntryView(item: GlobalRosterEntry): GlobalRosterEntryView {
    return GlobalRosterEntryView(
        codeText = truncate(item.code, 40),
        nameText = truncate(item.name, 64),
        discount = "%.2f".format(item.discount),
        floorValue = "%.2f".format(item.floorValue),
        weight = "%.2f".format(item.weight),
        trailerIdText = truncate(item.trailerId, 120),
        currency = item.currency,
        attempts = item.attempts.toString(),
        externalRefText = truncate(item.externalRef, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalRosterEntryViews(items: MutableList<GlobalRosterEntry>): MutableList<GlobalRosterEntryView> {
    val views = mutableListOf<GlobalRosterEntryView>()
    for (item in items) {
        views.add(toGlobalRosterEntryView(item))
    }
    return views
}

fun sumGlobalRosterEntryView(view: GlobalRosterEntryView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedWeight = view.weight.toDouble()
    return parsedDiscount + parsedFloorValue + parsedWeight
}
