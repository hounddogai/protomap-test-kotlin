package com.acme.transport.linehaul.trailer.policy

/** How screens and exports show a trailer bundle. */
data class TrailerBundleView(
    var code: String,
    var name: String,
    var active: String,
    var stopCount: String,
    var amount: String,
    var statusLabel: String,
)

fun toTrailerBundleView(item: TrailerBundle): TrailerBundleView {
    return TrailerBundleView(
        code = item.code,
        name = item.name,
        active = "${item.active}",
        stopCount = item.stopCount.toString(),
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerBundleViews(items: MutableList<TrailerBundle>): MutableList<TrailerBundleView> {
    val views = mutableListOf<TrailerBundleView>()
    for (item in items) {
        views.add(toTrailerBundleView(item))
    }
    return views
}

fun sumTrailerBundleView(view: TrailerBundleView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedAmount = view.amount.toDouble()
    return parsedStopCount.toDouble() + parsedAmount
}
