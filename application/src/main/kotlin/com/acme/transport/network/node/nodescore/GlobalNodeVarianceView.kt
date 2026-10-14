package com.acme.transport.network.node.nodescore

/** How screens and exports show a global node variance. */
data class GlobalNodeVarianceView(
    var code: String,
    var titleText: String,
    var maximumText: String,
    var summaryText: String,
    var lane: String,
    var dock: String,
    var capacityText: String,
    var pallets: String,
    var active: String,
    var statusLabel: String,
)

fun toGlobalNodeVarianceView(item: GlobalNodeVariance): GlobalNodeVarianceView {
    return GlobalNodeVarianceView(
        code = item.code,
        titleText = item.title,
        maximumText = "%.2f".format(item.maximum),
        summaryText = item.summary,
        lane = item.lane,
        dock = item.dock,
        capacityText = item.capacity.toString(),
        pallets = item.pallets.toString(),
        active = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalNodeVarianceViews(items: MutableList<GlobalNodeVariance>): MutableList<GlobalNodeVarianceView> {
    val views = mutableListOf<GlobalNodeVarianceView>()
    for (item in items) {
        views.add(toGlobalNodeVarianceView(item))
    }
    return views
}

fun sumGlobalNodeVarianceView(view: GlobalNodeVarianceView): Double {
    val parsedMaximum = view.maximumText.toDouble()
    val parsedCapacity = view.capacityText.toInt()
    val parsedPallets = view.pallets.toInt()
    return parsedMaximum + parsedCapacity.toDouble() + parsedPallets.toDouble()
}
