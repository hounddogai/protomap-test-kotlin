package com.acme.transport.customs.hold.group

/** How screens and exports show a default hold rollup. */
data class DefaultHoldRollupView(
    var code: String,
    var title: String,
    var odometer: String,
    var rateText: String,
    var etaMinutes: String,
    var contactEmail: String,
    var summaryText: String,
    var channelText: String,
    var statusLabel: String,
)

fun toDefaultHoldRollupView(item: DefaultHoldRollup): DefaultHoldRollupView {
    return DefaultHoldRollupView(
        code = item.code,
        title = item.title,
        odometer = item.odometer.toString(),
        rateText = "%.2f".format(item.rate),
        etaMinutes = item.etaMinutes.toString(),
        contactEmail = item.contactEmail,
        summaryText = item.summary,
        channelText = item.channel,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultHoldRollupViews(items: MutableList<DefaultHoldRollup>): MutableList<DefaultHoldRollupView> {
    val views = mutableListOf<DefaultHoldRollupView>()
    for (item in items) {
        views.add(toDefaultHoldRollupView(item))
    }
    return views
}

fun sumDefaultHoldRollupView(view: DefaultHoldRollupView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedRate = view.rateText.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedOdometer.toDouble() + parsedRate + parsedEtaMinutes.toDouble()
}
