package com.acme.transport.network.cutoff.estimate

/** How screens and exports show a weighted cutoff estimate. */
data class WeightedCutoffEstimateView(
    var code: String,
    var label: String,
    var approvedText: String,
    var shiftCodeText: String,
    var startsAt: String,
    var visible: String,
    var pinned: String,
    var cost: String,
    var statusLabel: String,
)

fun toWeightedCutoffEstimateView(item: WeightedCutoffEstimate): WeightedCutoffEstimateView {
    return WeightedCutoffEstimateView(
        code = item.code,
        label = item.label,
        approvedText = "${item.approved}",
        shiftCodeText = item.shiftCode,
        startsAt = item.startsAt.toString(),
        visible = "${item.visible}",
        pinned = "${item.pinned}",
        cost = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedCutoffEstimateViews(items: MutableList<WeightedCutoffEstimate>): MutableList<WeightedCutoffEstimateView> {
    val views = mutableListOf<WeightedCutoffEstimateView>()
    for (item in items) {
        views.add(toWeightedCutoffEstimateView(item))
    }
    return views
}

fun sumWeightedCutoffEstimateView(view: WeightedCutoffEstimateView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedCost = view.cost.toDouble()
    return parsedStartsAt.toDouble() + parsedCost
}
