package com.acme.transport.linehaul.backhaul.backhaulassignment

/** How screens and exports show a tiered backhaul rollup. */
data class TieredBackhaulRollupView(
    var code: String,
    var nameText: String,
    var scoreText: String,
    var vehicleId: String,
    var limit: String,
    var country: String,
    var updatedAtText: String,
    var routeCodeText: String,
    var region: String,
    var ceiling: String,
    var stopCountText: String,
    var statusLabel: String,
)

fun toTieredBackhaulRollupView(item: TieredBackhaulRollup): TieredBackhaulRollupView {
    return TieredBackhaulRollupView(
        code = item.code,
        nameText = item.name,
        scoreText = "%.2f".format(item.score),
        vehicleId = item.vehicleId,
        limit = item.limit.toString(),
        country = item.country,
        updatedAtText = item.updatedAt.toString(),
        routeCodeText = item.routeCode,
        region = item.region,
        ceiling = "%.2f".format(item.ceiling),
        stopCountText = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredBackhaulRollupViews(items: MutableList<TieredBackhaulRollup>): MutableList<TieredBackhaulRollupView> {
    val views = mutableListOf<TieredBackhaulRollupView>()
    for (item in items) {
        views.add(toTieredBackhaulRollupView(item))
    }
    return views
}

fun sumTieredBackhaulRollupView(view: TieredBackhaulRollupView): Double {
    val parsedScore = view.scoreText.toDouble()
    val parsedLimit = view.limit.toInt()
    val parsedUpdatedAt = view.updatedAtText.toInt()
    return parsedScore + parsedLimit.toDouble() + parsedUpdatedAt.toDouble()
}
