package com.acme.transport.customs.broker.profile

/** How screens and exports show a pending broker policy. */
data class PendingBrokerPolicyView(
    var code: String,
    var title: String,
    var limit: String,
    var routeCode: String,
    var budget: String,
    var statusLabel: String,
)

fun toPendingBrokerPolicyView(item: PendingBrokerPolicy): PendingBrokerPolicyView {
    return PendingBrokerPolicyView(
        code = item.code,
        title = item.title,
        limit = item.limit.toString(),
        routeCode = item.routeCode,
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingBrokerPolicyViews(items: MutableList<PendingBrokerPolicy>): MutableList<PendingBrokerPolicyView> {
    val views = mutableListOf<PendingBrokerPolicyView>()
    for (item in items) {
        views.add(toPendingBrokerPolicyView(item))
    }
    return views
}

fun sumPendingBrokerPolicyView(view: PendingBrokerPolicyView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedBudget = view.budget.toDouble()
    return parsedLimit.toDouble() + parsedBudget
}
