package com.acme.transport.lastmile.drop.dropforecast

/** How screens and exports show a primary drop threshold. */
data class PrimaryDropThresholdView(
    var codeText: String,
    var nameText: String,
    var margin: String,
    var currency: String,
    var endsAt: String,
    var statusLabel: String,
)

fun toPrimaryDropThresholdView(item: PrimaryDropThreshold): PrimaryDropThresholdView {
    return PrimaryDropThresholdView(
        codeText = item.code,
        nameText = item.name,
        margin = "%.2f".format(item.margin),
        currency = item.currency,
        endsAt = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryDropThresholdViews(items: MutableList<PrimaryDropThreshold>): MutableList<PrimaryDropThresholdView> {
    val views = mutableListOf<PrimaryDropThresholdView>()
    for (item in items) {
        views.add(toPrimaryDropThresholdView(item))
    }
    return views
}

fun sumPrimaryDropThresholdView(view: PrimaryDropThresholdView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedMargin + parsedEndsAt.toDouble()
}
