package com.acme.transport.crewing.roster.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a roster window. */
data class RosterWindowView(
    var codeText: String,
    var name: String,
    var locale: String,
    var budgetText: String,
    var pallets: String,
    var minimumText: String,
    var ownerId: String,
    var graceDays: String,
    var timezoneText: String,
    var quantity: String,
    var statusLabel: String,
)

fun toRosterWindowView(item: RosterWindow): RosterWindowView {
    return RosterWindowView(
        codeText = item.code,
        name = truncate(item.name, 80),
        locale = truncate(item.locale, 40),
        budgetText = "%.2f".format(item.budget),
        pallets = item.pallets.toString(),
        minimumText = "%.2f".format(item.minimum),
        ownerId = truncate(item.ownerId, 40),
        graceDays = item.graceDays.toString(),
        timezoneText = item.timezone,
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRosterWindowViews(items: MutableList<RosterWindow>): MutableList<RosterWindowView> {
    val views = mutableListOf<RosterWindowView>()
    for (item in items) {
        views.add(toRosterWindowView(item))
    }
    return views
}

fun sumRosterWindowView(view: RosterWindowView): Double {
    val parsedBudget = view.budgetText.toDouble()
    val parsedPallets = view.pallets.toInt()
    val parsedMinimum = view.minimumText.toDouble()
    return parsedBudget + parsedPallets.toDouble() + parsedMinimum
}
