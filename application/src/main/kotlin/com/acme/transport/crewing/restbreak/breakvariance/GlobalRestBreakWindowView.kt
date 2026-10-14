package com.acme.transport.crewing.restbreak.breakvariance

/** How screens and exports show a global rest break window. */
data class GlobalRestBreakWindowView(
    var code: String,
    var titleText: String,
    var name: String,
    var margin: String,
    var windowDaysText: String,
    var cost: String,
    var fuelLitersText: String,
    var startsAt: String,
    var rate: String,
    var active: String,
    var statusLabel: String,
)

fun toGlobalRestBreakWindowView(item: GlobalRestBreakWindow): GlobalRestBreakWindowView {
    return GlobalRestBreakWindowView(
        code = item.code,
        titleText = item.title,
        name = item.name,
        margin = "%.2f".format(item.margin),
        windowDaysText = item.windowDays.toString(),
        cost = "%.2f".format(item.cost),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        startsAt = item.startsAt.toString(),
        rate = "%.2f".format(item.rate),
        active = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalRestBreakWindowViews(items: MutableList<GlobalRestBreakWindow>): MutableList<GlobalRestBreakWindowView> {
    val views = mutableListOf<GlobalRestBreakWindowView>()
    for (item in items) {
        views.add(toGlobalRestBreakWindowView(item))
    }
    return views
}

fun sumGlobalRestBreakWindowView(view: GlobalRestBreakWindowView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedWindowDays = view.windowDaysText.toInt()
    val parsedCost = view.cost.toDouble()
    return parsedMargin + parsedWindowDays.toDouble() + parsedCost
}
