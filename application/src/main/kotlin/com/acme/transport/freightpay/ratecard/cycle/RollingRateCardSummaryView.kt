package com.acme.transport.freightpay.ratecard.cycle

/** How screens and exports show a rolling rate card summary. */
data class RollingRateCardSummaryView(
    var code: String,
    var label: String,
    var amount: String,
    var stopCountText: String,
    var ratioText: String,
    var pinnedText: String,
    var budget: String,
    var statusLabel: String,
)

fun toRollingRateCardSummaryView(item: RollingRateCardSummary): RollingRateCardSummaryView {
    return RollingRateCardSummaryView(
        code = item.code,
        label = item.label,
        amount = "%.2f".format(item.amount),
        stopCountText = item.stopCount.toString(),
        ratioText = "%.2f".format(item.ratio),
        pinnedText = "${item.pinned}",
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingRateCardSummaryViews(items: MutableList<RollingRateCardSummary>): MutableList<RollingRateCardSummaryView> {
    val views = mutableListOf<RollingRateCardSummaryView>()
    for (item in items) {
        views.add(toRollingRateCardSummaryView(item))
    }
    return views
}

fun sumRollingRateCardSummaryView(view: RollingRateCardSummaryView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedStopCount = view.stopCountText.toInt()
    val parsedRatio = view.ratioText.toDouble()
    return parsedAmount + parsedStopCount.toDouble() + parsedRatio
}
