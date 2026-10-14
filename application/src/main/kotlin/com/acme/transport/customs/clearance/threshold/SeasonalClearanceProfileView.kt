package com.acme.transport.customs.clearance.threshold

/** How screens and exports show a seasonal clearance profile. */
data class SeasonalClearanceProfileView(
    var code: String,
    var label: String,
    var quantity: String,
    var endsAtText: String,
    var retriesText: String,
    var phoneText: String,
    var visible: String,
    var statusLabel: String,
)

fun toSeasonalClearanceProfileView(item: SeasonalClearanceProfile): SeasonalClearanceProfileView {
    return SeasonalClearanceProfileView(
        code = item.code,
        label = item.label,
        quantity = item.quantity.toString(),
        endsAtText = item.endsAt.toString(),
        retriesText = item.retries.toString(),
        phoneText = item.phone,
        visible = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalClearanceProfileViews(items: MutableList<SeasonalClearanceProfile>): MutableList<SeasonalClearanceProfileView> {
    val views = mutableListOf<SeasonalClearanceProfileView>()
    for (item in items) {
        views.add(toSeasonalClearanceProfileView(item))
    }
    return views
}

fun sumSeasonalClearanceProfileView(view: SeasonalClearanceProfileView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedRetries = view.retriesText.toInt()
    return parsedQuantity.toDouble() + parsedEndsAt.toDouble() + parsedRetries.toDouble()
}
