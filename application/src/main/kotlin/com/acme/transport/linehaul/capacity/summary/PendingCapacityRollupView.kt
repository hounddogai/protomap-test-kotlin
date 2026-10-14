package com.acme.transport.linehaul.capacity.summary

/** How screens and exports show a pending capacity rollup. */
data class PendingCapacityRollupView(
    var code: String,
    var titleText: String,
    var visible: String,
    var priority: String,
    var activeText: String,
    var margin: String,
    var pallets: String,
    var graceDaysText: String,
    var windowDaysText: String,
    var costText: String,
    var statusLabel: String,
)

fun toPendingCapacityRollupView(item: PendingCapacityRollup): PendingCapacityRollupView {
    return PendingCapacityRollupView(
        code = item.code,
        titleText = item.title,
        visible = "${item.visible}",
        priority = item.priority.toString(),
        activeText = "${item.active}",
        margin = "%.2f".format(item.margin),
        pallets = item.pallets.toString(),
        graceDaysText = item.graceDays.toString(),
        windowDaysText = item.windowDays.toString(),
        costText = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingCapacityRollupViews(items: MutableList<PendingCapacityRollup>): MutableList<PendingCapacityRollupView> {
    val views = mutableListOf<PendingCapacityRollupView>()
    for (item in items) {
        views.add(toPendingCapacityRollupView(item))
    }
    return views
}

fun sumPendingCapacityRollupView(view: PendingCapacityRollupView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedMargin = view.margin.toDouble()
    val parsedPallets = view.pallets.toInt()
    return parsedPriority.toDouble() + parsedMargin + parsedPallets.toDouble()
}
