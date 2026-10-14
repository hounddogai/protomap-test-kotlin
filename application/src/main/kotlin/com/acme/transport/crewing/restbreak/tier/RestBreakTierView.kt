package com.acme.transport.crewing.restbreak.tier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rest break tier. */
data class RestBreakTierView(
    var code: String,
    var nameText: String,
    var vehicleId: String,
    var channel: String,
    var score: String,
    var cost: String,
    var statusLabel: String,
)

fun toRestBreakTierView(item: RestBreakTier): RestBreakTierView {
    return RestBreakTierView(
        code = truncate(item.code, 40),
        nameText = item.name,
        vehicleId = item.vehicleId,
        channel = item.channel,
        score = "%.2f".format(item.score),
        cost = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRestBreakTierViews(items: MutableList<RestBreakTier>): MutableList<RestBreakTierView> {
    val views = mutableListOf<RestBreakTierView>()
    for (item in items) {
        views.add(toRestBreakTierView(item))
    }
    return views
}

fun sumRestBreakTierView(view: RestBreakTierView): Double {
    val parsedScore = view.score.toDouble()
    val parsedCost = view.cost.toDouble()
    return parsedScore + parsedCost
}
