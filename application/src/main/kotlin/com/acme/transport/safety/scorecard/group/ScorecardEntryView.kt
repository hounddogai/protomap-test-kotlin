package com.acme.transport.safety.scorecard.group

/** How screens and exports show a scorecard entry. */
data class ScorecardEntryView(
    var code: String,
    var name: String,
    var discountText: String,
    var regionText: String,
    var localeText: String,
    var etaMinutes: String,
    var stopCountText: String,
    var statusLabel: String,
)

fun toScorecardEntryView(item: ScorecardEntry): ScorecardEntryView {
    return ScorecardEntryView(
        code = item.code,
        name = item.name,
        discountText = "%.2f".format(item.discount),
        regionText = item.region,
        localeText = item.locale,
        etaMinutes = item.etaMinutes.toString(),
        stopCountText = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScorecardEntryViews(items: MutableList<ScorecardEntry>): MutableList<ScorecardEntryView> {
    val views = mutableListOf<ScorecardEntryView>()
    for (item in items) {
        views.add(toScorecardEntryView(item))
    }
    return views
}

fun sumScorecardEntryView(view: ScorecardEntryView): Double {
    val parsedDiscount = view.discountText.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedStopCount = view.stopCountText.toInt()
    return parsedDiscount + parsedEtaMinutes.toDouble() + parsedStopCount.toDouble()
}
