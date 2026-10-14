package com.acme.transport.crewing.restbreak.breakrollup

/** How screens and exports show a rolling rest break rollup. */
data class RollingRestBreakRollupView(
    var code: String,
    var title: String,
    var archived: String,
    var dock: String,
    var ceiling: String,
    var phone: String,
    var active: String,
    var country: String,
    var floorValue: String,
    var depot: String,
    var discount: String,
    var statusLabel: String,
)

fun toRollingRestBreakRollupView(item: RollingRestBreakRollup): RollingRestBreakRollupView {
    return RollingRestBreakRollupView(
        code = item.code,
        title = item.title,
        archived = "${item.archived}",
        dock = item.dock,
        ceiling = "%.2f".format(item.ceiling),
        phone = item.phone,
        active = "${item.active}",
        country = item.country,
        floorValue = "%.2f".format(item.floorValue),
        depot = item.depot,
        discount = "%.2f".format(item.discount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingRestBreakRollupViews(items: MutableList<RollingRestBreakRollup>): MutableList<RollingRestBreakRollupView> {
    val views = mutableListOf<RollingRestBreakRollupView>()
    for (item in items) {
        views.add(toRollingRestBreakRollupView(item))
    }
    return views
}

fun sumRollingRestBreakRollupView(view: RollingRestBreakRollupView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedDiscount = view.discount.toDouble()
    return parsedCeiling + parsedFloorValue + parsedDiscount
}
