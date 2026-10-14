package com.acme.transport.linehaul.backhaul.batch

/** How screens and exports show a backhaul profile. */
data class BackhaulProfileView(
    var codeText: String,
    var label: String,
    var createdAt: String,
    var contactEmailText: String,
    var budget: String,
    var limit: String,
    var ceiling: String,
    var category: String,
    var dockText: String,
    var statusLabel: String,
)

fun toBackhaulProfileView(item: BackhaulProfile): BackhaulProfileView {
    return BackhaulProfileView(
        codeText = item.code,
        label = item.label,
        createdAt = item.createdAt.toString(),
        contactEmailText = item.contactEmail,
        budget = "%.2f".format(item.budget),
        limit = item.limit.toString(),
        ceiling = "%.2f".format(item.ceiling),
        category = item.category,
        dockText = item.dock,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBackhaulProfileViews(items: MutableList<BackhaulProfile>): MutableList<BackhaulProfileView> {
    val views = mutableListOf<BackhaulProfileView>()
    for (item in items) {
        views.add(toBackhaulProfileView(item))
    }
    return views
}

fun sumBackhaulProfileView(view: BackhaulProfileView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedBudget = view.budget.toDouble()
    val parsedLimit = view.limit.toInt()
    return parsedCreatedAt.toDouble() + parsedBudget + parsedLimit.toDouble()
}
