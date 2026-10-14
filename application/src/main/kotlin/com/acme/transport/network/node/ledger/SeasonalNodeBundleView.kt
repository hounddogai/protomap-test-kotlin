package com.acme.transport.network.node.ledger

/** How screens and exports show a seasonal node bundle. */
data class SeasonalNodeBundleView(
    var code: String,
    var title: String,
    var pallets: String,
    var weightText: String,
    var revision: String,
    var ceiling: String,
    var quantity: String,
    var name: String,
    var floorValue: String,
    var carrier: String,
    var statusLabel: String,
)

fun toSeasonalNodeBundleView(item: SeasonalNodeBundle): SeasonalNodeBundleView {
    return SeasonalNodeBundleView(
        code = item.code,
        title = item.title,
        pallets = item.pallets.toString(),
        weightText = "%.2f".format(item.weight),
        revision = item.revision.toString(),
        ceiling = "%.2f".format(item.ceiling),
        quantity = item.quantity.toString(),
        name = item.name,
        floorValue = "%.2f".format(item.floorValue),
        carrier = item.carrier,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalNodeBundleViews(items: MutableList<SeasonalNodeBundle>): MutableList<SeasonalNodeBundleView> {
    val views = mutableListOf<SeasonalNodeBundleView>()
    for (item in items) {
        views.add(toSeasonalNodeBundleView(item))
    }
    return views
}

fun sumSeasonalNodeBundleView(view: SeasonalNodeBundleView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedWeight = view.weightText.toDouble()
    val parsedRevision = view.revision.toInt()
    return parsedPallets.toDouble() + parsedWeight + parsedRevision.toDouble()
}
