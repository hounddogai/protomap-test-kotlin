package com.acme.transport.fleetops.leasereturn.entry

/** How screens and exports show a regional lease return entry. */
data class RegionalLeaseReturnEntryView(
    var codeText: String,
    var name: String,
    var graceDays: String,
    var ratio: String,
    var dockText: String,
    var title: String,
    var statusLabel: String,
)

fun toRegionalLeaseReturnEntryView(item: RegionalLeaseReturnEntry): RegionalLeaseReturnEntryView {
    return RegionalLeaseReturnEntryView(
        codeText = item.code,
        name = item.name,
        graceDays = item.graceDays.toString(),
        ratio = "%.2f".format(item.ratio),
        dockText = item.dock,
        title = item.title,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalLeaseReturnEntryViews(items: MutableList<RegionalLeaseReturnEntry>): MutableList<RegionalLeaseReturnEntryView> {
    val views = mutableListOf<RegionalLeaseReturnEntryView>()
    for (item in items) {
        views.add(toRegionalLeaseReturnEntryView(item))
    }
    return views
}

fun sumRegionalLeaseReturnEntryView(view: RegionalLeaseReturnEntryView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedRatio = view.ratio.toDouble()
    return parsedGraceDays.toDouble() + parsedRatio
}
