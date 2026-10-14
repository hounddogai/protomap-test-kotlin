package com.acme.transport.lastmile.drop.rollup

/** How screens and exports show a drop rollup. */
data class DropRollupView(
    var code: String,
    var name: String,
    var limitText: String,
    var weight: String,
    var rate: String,
    var startsAtText: String,
    var marginText: String,
    var fuelLitersText: String,
    var statusLabel: String,
)

fun toDropRollupView(item: DropRollup): DropRollupView {
    return DropRollupView(
        code = item.code,
        name = item.name,
        limitText = item.limit.toString(),
        weight = "%.2f".format(item.weight),
        rate = "%.2f".format(item.rate),
        startsAtText = item.startsAt.toString(),
        marginText = "%.2f".format(item.margin),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDropRollupViews(items: MutableList<DropRollup>): MutableList<DropRollupView> {
    val views = mutableListOf<DropRollupView>()
    for (item in items) {
        views.add(toDropRollupView(item))
    }
    return views
}

fun sumDropRollupView(view: DropRollupView): Double {
    val parsedLimit = view.limitText.toInt()
    val parsedWeight = view.weight.toDouble()
    val parsedRate = view.rate.toDouble()
    return parsedLimit.toDouble() + parsedWeight + parsedRate
}
