package com.acme.transport.lastmile.parcel.parcelbaseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped parcel quota. */
data class CappedParcelQuotaView(
    var codeText: String,
    var title: String,
    var timezoneText: String,
    var rateText: String,
    var priorityText: String,
    var weightText: String,
    var unit: String,
    var durationMinutesText: String,
    var graceDays: String,
    var label: String,
    var quantityText: String,
    var statusLabel: String,
)

fun toCappedParcelQuotaView(item: CappedParcelQuota): CappedParcelQuotaView {
    return CappedParcelQuotaView(
        codeText = truncate(item.code, 80),
        title = item.title,
        timezoneText = item.timezone,
        rateText = "%.2f".format(item.rate),
        priorityText = item.priority.toString(),
        weightText = "%.2f".format(item.weight),
        unit = item.unit,
        durationMinutesText = item.durationMinutes.toString(),
        graceDays = item.graceDays.toString(),
        label = item.label,
        quantityText = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedParcelQuotaViews(items: MutableList<CappedParcelQuota>): MutableList<CappedParcelQuotaView> {
    val views = mutableListOf<CappedParcelQuotaView>()
    for (item in items) {
        views.add(toCappedParcelQuotaView(item))
    }
    return views
}

fun sumCappedParcelQuotaView(view: CappedParcelQuotaView): Double {
    val parsedRate = view.rateText.toDouble()
    val parsedPriority = view.priorityText.toInt()
    val parsedWeight = view.weightText.toDouble()
    return parsedRate + parsedPriority.toDouble() + parsedWeight
}
