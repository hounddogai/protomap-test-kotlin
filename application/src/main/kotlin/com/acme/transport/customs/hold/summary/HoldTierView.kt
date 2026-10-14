package com.acme.transport.customs.hold.summary

/** How screens and exports show a hold tier. */
data class HoldTierView(
    var codeText: String,
    var titleText: String,
    var summary: String,
    var timezone: String,
    var ratio: String,
    var graceDaysText: String,
    var statusLabel: String,
)

fun toHoldTierView(item: HoldTier): HoldTierView {
    return HoldTierView(
        codeText = item.code,
        titleText = item.title,
        summary = item.summary,
        timezone = item.timezone,
        ratio = "%.2f".format(item.ratio),
        graceDaysText = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHoldTierViews(items: MutableList<HoldTier>): MutableList<HoldTierView> {
    val views = mutableListOf<HoldTierView>()
    for (item in items) {
        views.add(toHoldTierView(item))
    }
    return views
}

fun sumHoldTierView(view: HoldTierView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedGraceDays = view.graceDaysText.toInt()
    return parsedRatio + parsedGraceDays.toDouble()
}
