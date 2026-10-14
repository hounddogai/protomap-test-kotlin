package com.acme.transport.terminal.crossdock.strictdockslot

/** How screens and exports show a legacy cross dock entry. */
data class LegacyCrossDockEntryView(
    var codeText: String,
    var labelText: String,
    var budget: String,
    var dock: String,
    var endsAt: String,
    var fuelLiters: String,
    var driverId: String,
    var statusLabel: String,
)

fun toLegacyCrossDockEntryView(item: LegacyCrossDockEntry): LegacyCrossDockEntryView {
    return LegacyCrossDockEntryView(
        codeText = item.code,
        labelText = item.label,
        budget = "%.2f".format(item.budget),
        dock = item.dock,
        endsAt = item.endsAt.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        driverId = item.driverId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyCrossDockEntryViews(items: MutableList<LegacyCrossDockEntry>): MutableList<LegacyCrossDockEntryView> {
    val views = mutableListOf<LegacyCrossDockEntryView>()
    for (item in items) {
        views.add(toLegacyCrossDockEntryView(item))
    }
    return views
}

fun sumLegacyCrossDockEntryView(view: LegacyCrossDockEntryView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedBudget + parsedEndsAt.toDouble() + parsedFuelLiters
}
