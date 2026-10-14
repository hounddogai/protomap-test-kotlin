package com.acme.transport.fleetops.tire.preference

/** How screens and exports show a tire preference. */
data class TirePreferenceView(
    var code: String,
    var label: String,
    var stopCount: String,
    var visible: String,
    var amount: String,
    var costText: String,
    var maximum: String,
    var statusLabel: String,
)

fun toTirePreferenceView(item: TirePreference): TirePreferenceView {
    return TirePreferenceView(
        code = item.code,
        label = item.label,
        stopCount = item.stopCount.toString(),
        visible = "${item.visible}",
        amount = "%.2f".format(item.amount),
        costText = "%.2f".format(item.cost),
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTirePreferenceViews(items: MutableList<TirePreference>): MutableList<TirePreferenceView> {
    val views = mutableListOf<TirePreferenceView>()
    for (item in items) {
        views.add(toTirePreferenceView(item))
    }
    return views
}

fun sumTirePreferenceView(view: TirePreferenceView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedAmount = view.amount.toDouble()
    val parsedCost = view.costText.toDouble()
    return parsedStopCount.toDouble() + parsedAmount + parsedCost
}
