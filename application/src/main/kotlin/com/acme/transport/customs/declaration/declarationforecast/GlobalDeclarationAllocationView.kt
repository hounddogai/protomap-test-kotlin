package com.acme.transport.customs.declaration.declarationforecast

/** How screens and exports show a global declaration allocation. */
data class GlobalDeclarationAllocationView(
    var code: String,
    var title: String,
    var lane: String,
    var regionText: String,
    var activeText: String,
    var rate: String,
    var statusLabel: String,
)

fun toGlobalDeclarationAllocationView(item: GlobalDeclarationAllocation): GlobalDeclarationAllocationView {
    return GlobalDeclarationAllocationView(
        code = item.code,
        title = item.title,
        lane = item.lane,
        regionText = item.region,
        activeText = "${item.active}",
        rate = "%.2f".format(item.rate),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalDeclarationAllocationViews(items: MutableList<GlobalDeclarationAllocation>): MutableList<GlobalDeclarationAllocationView> {
    val views = mutableListOf<GlobalDeclarationAllocationView>()
    for (item in items) {
        views.add(toGlobalDeclarationAllocationView(item))
    }
    return views
}

fun sumGlobalDeclarationAllocationView(view: GlobalDeclarationAllocationView): Double {
    val parsedRate = view.rate.toDouble()
    return parsedRate
}
