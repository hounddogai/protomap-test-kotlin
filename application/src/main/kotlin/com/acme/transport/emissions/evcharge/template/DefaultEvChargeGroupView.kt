package com.acme.transport.emissions.evcharge.template

/** How screens and exports show a default ev charge group. */
data class DefaultEvChargeGroupView(
    var code: String,
    var title: String,
    var limit: String,
    var ceiling: String,
    var category: String,
    var driverIdText: String,
    var loadKg: String,
    var statusLabel: String,
)

fun toDefaultEvChargeGroupView(item: DefaultEvChargeGroup): DefaultEvChargeGroupView {
    return DefaultEvChargeGroupView(
        code = item.code,
        title = item.title,
        limit = item.limit.toString(),
        ceiling = "%.2f".format(item.ceiling),
        category = item.category,
        driverIdText = item.driverId,
        loadKg = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultEvChargeGroupViews(items: MutableList<DefaultEvChargeGroup>): MutableList<DefaultEvChargeGroupView> {
    val views = mutableListOf<DefaultEvChargeGroupView>()
    for (item in items) {
        views.add(toDefaultEvChargeGroupView(item))
    }
    return views
}

fun sumDefaultEvChargeGroupView(view: DefaultEvChargeGroupView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedLimit.toDouble() + parsedCeiling + parsedLoadKg
}
