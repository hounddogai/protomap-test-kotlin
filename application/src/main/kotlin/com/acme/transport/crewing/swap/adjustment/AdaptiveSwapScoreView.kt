package com.acme.transport.crewing.swap.adjustment

/** How screens and exports show a adaptive swap score. */
data class AdaptiveSwapScoreView(
    var code: String,
    var label: String,
    var startsAt: String,
    var endsAt: String,
    var cost: String,
    var segment: String,
    var retriesText: String,
    var floorValue: String,
    var statusLabel: String,
)

fun toAdaptiveSwapScoreView(item: AdaptiveSwapScore): AdaptiveSwapScoreView {
    return AdaptiveSwapScoreView(
        code = item.code,
        label = item.label,
        startsAt = item.startsAt.toString(),
        endsAt = item.endsAt.toString(),
        cost = "%.2f".format(item.cost),
        segment = item.segment,
        retriesText = item.retries.toString(),
        floorValue = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveSwapScoreViews(items: MutableList<AdaptiveSwapScore>): MutableList<AdaptiveSwapScoreView> {
    val views = mutableListOf<AdaptiveSwapScoreView>()
    for (item in items) {
        views.add(toAdaptiveSwapScoreView(item))
    }
    return views
}

fun sumAdaptiveSwapScoreView(view: AdaptiveSwapScoreView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedCost = view.cost.toDouble()
    return parsedStartsAt.toDouble() + parsedEndsAt.toDouble() + parsedCost
}
