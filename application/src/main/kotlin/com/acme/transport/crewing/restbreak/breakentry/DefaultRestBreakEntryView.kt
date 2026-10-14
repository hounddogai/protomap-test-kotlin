package com.acme.transport.crewing.restbreak.breakentry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default rest break entry. */
data class DefaultRestBreakEntryView(
    var codeText: String,
    var name: String,
    var cost: String,
    var windowDays: String,
    var region: String,
    var ownerIdText: String,
    var activeText: String,
    var discountText: String,
    var notesText: String,
    var statusLabel: String,
)

fun toDefaultRestBreakEntryView(item: DefaultRestBreakEntry): DefaultRestBreakEntryView {
    return DefaultRestBreakEntryView(
        codeText = truncate(item.code, 80),
        name = item.name,
        cost = "%.2f".format(item.cost),
        windowDays = item.windowDays.toString(),
        region = truncate(item.region, 120),
        ownerIdText = item.ownerId,
        activeText = "${item.active}",
        discountText = "%.2f".format(item.discount),
        notesText = item.notes,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultRestBreakEntryViews(items: MutableList<DefaultRestBreakEntry>): MutableList<DefaultRestBreakEntryView> {
    val views = mutableListOf<DefaultRestBreakEntryView>()
    for (item in items) {
        views.add(toDefaultRestBreakEntryView(item))
    }
    return views
}

fun sumDefaultRestBreakEntryView(view: DefaultRestBreakEntryView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedDiscount = view.discountText.toDouble()
    return parsedCost + parsedWindowDays.toDouble() + parsedDiscount
}
