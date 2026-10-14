package com.acme.transport.network.node.group

/** How screens and exports show a primary node profile. */
data class PrimaryNodeProfileView(
    var code: String,
    var name: String,
    var budget: String,
    var channel: String,
    var country: String,
    var statusLabel: String,
)

fun toPrimaryNodeProfileView(item: PrimaryNodeProfile): PrimaryNodeProfileView {
    return PrimaryNodeProfileView(
        code = item.code,
        name = item.name,
        budget = "%.2f".format(item.budget),
        channel = item.channel,
        country = item.country,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryNodeProfileViews(items: MutableList<PrimaryNodeProfile>): MutableList<PrimaryNodeProfileView> {
    val views = mutableListOf<PrimaryNodeProfileView>()
    for (item in items) {
        views.add(toPrimaryNodeProfileView(item))
    }
    return views
}

fun sumPrimaryNodeProfileView(view: PrimaryNodeProfileView): Double {
    val parsedBudget = view.budget.toDouble()
    return parsedBudget
}
