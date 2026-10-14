package com.acme.transport.network.node.nodeagreement

/** How screens and exports show a default node allocation. */
data class DefaultNodeAllocationView(
    var code: String,
    var title: String,
    var driverId: String,
    var margin: String,
    var limit: String,
    var routeCode: String,
    var palletsText: String,
    var cost: String,
    var currency: String,
    var activeText: String,
    var statusLabel: String,
)

fun toDefaultNodeAllocationView(item: DefaultNodeAllocation): DefaultNodeAllocationView {
    return DefaultNodeAllocationView(
        code = item.code,
        title = item.title,
        driverId = item.driverId,
        margin = "%.2f".format(item.margin),
        limit = item.limit.toString(),
        routeCode = item.routeCode,
        palletsText = item.pallets.toString(),
        cost = "%.2f".format(item.cost),
        currency = item.currency,
        activeText = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultNodeAllocationViews(items: MutableList<DefaultNodeAllocation>): MutableList<DefaultNodeAllocationView> {
    val views = mutableListOf<DefaultNodeAllocationView>()
    for (item in items) {
        views.add(toDefaultNodeAllocationView(item))
    }
    return views
}

fun sumDefaultNodeAllocationView(view: DefaultNodeAllocationView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedLimit = view.limit.toInt()
    val parsedPallets = view.palletsText.toInt()
    return parsedMargin + parsedLimit.toDouble() + parsedPallets.toDouble()
}
