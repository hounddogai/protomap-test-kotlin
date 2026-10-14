package com.acme.transport.linehaul.tender.window

/** How screens and exports show a adaptive tender window. */
data class AdaptiveTenderWindowView(
    var codeText: String,
    var title: String,
    var odometer: String,
    var ratioText: String,
    var summary: String,
    var budget: String,
    var statusLabel: String,
)

fun toAdaptiveTenderWindowView(item: AdaptiveTenderWindow): AdaptiveTenderWindowView {
    return AdaptiveTenderWindowView(
        codeText = item.code,
        title = item.title,
        odometer = item.odometer.toString(),
        ratioText = "%.2f".format(item.ratio),
        summary = item.summary,
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveTenderWindowViews(items: MutableList<AdaptiveTenderWindow>): MutableList<AdaptiveTenderWindowView> {
    val views = mutableListOf<AdaptiveTenderWindowView>()
    for (item in items) {
        views.add(toAdaptiveTenderWindowView(item))
    }
    return views
}

fun sumAdaptiveTenderWindowView(view: AdaptiveTenderWindowView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedRatio = view.ratioText.toDouble()
    val parsedBudget = view.budget.toDouble()
    return parsedOdometer.toDouble() + parsedRatio + parsedBudget
}
