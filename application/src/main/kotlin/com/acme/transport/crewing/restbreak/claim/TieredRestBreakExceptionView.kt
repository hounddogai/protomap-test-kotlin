package com.acme.transport.crewing.restbreak.claim

/** How screens and exports show a tiered rest break exception. */
data class TieredRestBreakExceptionView(
    var codeText: String,
    var label: String,
    var minimum: String,
    var active: String,
    var title: String,
    var maximumText: String,
    var fuelLiters: String,
    var ceiling: String,
    var capacity: String,
    var statusLabel: String,
)

fun toTieredRestBreakExceptionView(item: TieredRestBreakException): TieredRestBreakExceptionView {
    return TieredRestBreakExceptionView(
        codeText = item.code,
        label = item.label,
        minimum = "%.2f".format(item.minimum),
        active = "${item.active}",
        title = item.title,
        maximumText = "%.2f".format(item.maximum),
        fuelLiters = "%.2f".format(item.fuelLiters),
        ceiling = "%.2f".format(item.ceiling),
        capacity = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredRestBreakExceptionViews(items: MutableList<TieredRestBreakException>): MutableList<TieredRestBreakExceptionView> {
    val views = mutableListOf<TieredRestBreakExceptionView>()
    for (item in items) {
        views.add(toTieredRestBreakExceptionView(item))
    }
    return views
}

fun sumTieredRestBreakExceptionView(view: TieredRestBreakExceptionView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedMaximum = view.maximumText.toDouble()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedMinimum + parsedMaximum + parsedFuelLiters
}
