package com.acme.transport.crewing.swap.entry

/** How screens and exports show a global swap override. */
data class GlobalSwapOverrideView(
    var code: String,
    var label: String,
    var approved: String,
    var retries: String,
    var cost: String,
    var vehicleId: String,
    var depot: String,
    var statusLabel: String,
)

fun toGlobalSwapOverrideView(item: GlobalSwapOverride): GlobalSwapOverrideView {
    return GlobalSwapOverrideView(
        code = item.code,
        label = item.label,
        approved = "${item.approved}",
        retries = item.retries.toString(),
        cost = "%.2f".format(item.cost),
        vehicleId = item.vehicleId,
        depot = item.depot,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalSwapOverrideViews(items: MutableList<GlobalSwapOverride>): MutableList<GlobalSwapOverrideView> {
    val views = mutableListOf<GlobalSwapOverrideView>()
    for (item in items) {
        views.add(toGlobalSwapOverrideView(item))
    }
    return views
}

fun sumGlobalSwapOverrideView(view: GlobalSwapOverrideView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedCost = view.cost.toDouble()
    return parsedRetries.toDouble() + parsedCost
}
