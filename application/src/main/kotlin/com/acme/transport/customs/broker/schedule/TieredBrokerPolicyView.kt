package com.acme.transport.customs.broker.schedule

/** How screens and exports show a tiered broker policy. */
data class TieredBrokerPolicyView(
    var code: String,
    var title: String,
    var limit: String,
    var trailerId: String,
    var budget: String,
    var checksumText: String,
    var statusLabel: String,
)

fun toTieredBrokerPolicyView(item: TieredBrokerPolicy): TieredBrokerPolicyView {
    return TieredBrokerPolicyView(
        code = item.code,
        title = item.title,
        limit = item.limit.toString(),
        trailerId = item.trailerId,
        budget = "%.2f".format(item.budget),
        checksumText = item.checksum,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredBrokerPolicyViews(items: MutableList<TieredBrokerPolicy>): MutableList<TieredBrokerPolicyView> {
    val views = mutableListOf<TieredBrokerPolicyView>()
    for (item in items) {
        views.add(toTieredBrokerPolicyView(item))
    }
    return views
}

fun sumTieredBrokerPolicyView(view: TieredBrokerPolicyView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedBudget = view.budget.toDouble()
    return parsedLimit.toDouble() + parsedBudget
}
