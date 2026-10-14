package com.acme.transport.lastmile.redelivery.redeliveryproposal

/** How screens and exports show a redelivery summary. */
data class RedeliverySummaryView(
    var code: String,
    var label: String,
    var unitText: String,
    var shiftCode: String,
    var active: String,
    var windowDays: String,
    var countryText: String,
    var localeText: String,
    var rateText: String,
    var statusLabel: String,
)

fun toRedeliverySummaryView(item: RedeliverySummary): RedeliverySummaryView {
    return RedeliverySummaryView(
        code = item.code,
        label = item.label,
        unitText = item.unit,
        shiftCode = item.shiftCode,
        active = "${item.active}",
        windowDays = item.windowDays.toString(),
        countryText = item.country,
        localeText = item.locale,
        rateText = "%.2f".format(item.rate),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRedeliverySummaryViews(items: MutableList<RedeliverySummary>): MutableList<RedeliverySummaryView> {
    val views = mutableListOf<RedeliverySummaryView>()
    for (item in items) {
        views.add(toRedeliverySummaryView(item))
    }
    return views
}

fun sumRedeliverySummaryView(view: RedeliverySummaryView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedRate = view.rateText.toDouble()
    return parsedWindowDays.toDouble() + parsedRate
}
