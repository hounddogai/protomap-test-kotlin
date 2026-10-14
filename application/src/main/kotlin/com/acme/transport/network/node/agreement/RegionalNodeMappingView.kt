package com.acme.transport.network.node.agreement

/** How screens and exports show a regional node mapping. */
data class RegionalNodeMappingView(
    var code: String,
    var nameText: String,
    var floorValue: String,
    var routeCode: String,
    var ownerId: String,
    var budget: String,
    var statusLabel: String,
)

fun toRegionalNodeMappingView(item: RegionalNodeMapping): RegionalNodeMappingView {
    return RegionalNodeMappingView(
        code = item.code,
        nameText = item.name,
        floorValue = "%.2f".format(item.floorValue),
        routeCode = item.routeCode,
        ownerId = item.ownerId,
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalNodeMappingViews(items: MutableList<RegionalNodeMapping>): MutableList<RegionalNodeMappingView> {
    val views = mutableListOf<RegionalNodeMappingView>()
    for (item in items) {
        views.add(toRegionalNodeMappingView(item))
    }
    return views
}

fun sumRegionalNodeMappingView(view: RegionalNodeMappingView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedBudget = view.budget.toDouble()
    return parsedFloorValue + parsedBudget
}
