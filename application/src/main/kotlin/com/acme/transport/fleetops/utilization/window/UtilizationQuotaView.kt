package com.acme.transport.fleetops.utilization.window

import com.acme.transport.kit.text.truncate

/** How screens and exports show a utilization quota. */
data class UtilizationQuotaView(
    var codeText: String,
    var labelText: String,
    var durationMinutesText: String,
    var loadKgText: String,
    var threshold: String,
    var phoneText: String,
    var fuelLitersText: String,
    var statusLabel: String,
)

fun toUtilizationQuotaView(item: UtilizationQuota): UtilizationQuotaView {
    return UtilizationQuotaView(
        codeText = item.code,
        labelText = item.label,
        durationMinutesText = item.durationMinutes.toString(),
        loadKgText = "%.2f".format(item.loadKg),
        threshold = "%.2f".format(item.threshold),
        phoneText = truncate(item.phone, 80),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toUtilizationQuotaViews(items: MutableList<UtilizationQuota>): MutableList<UtilizationQuotaView> {
    val views = mutableListOf<UtilizationQuotaView>()
    for (item in items) {
        views.add(toUtilizationQuotaView(item))
    }
    return views
}

fun sumUtilizationQuotaView(view: UtilizationQuotaView): Double {
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    val parsedLoadKg = view.loadKgText.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    return parsedDurationMinutes.toDouble() + parsedLoadKg + parsedThreshold
}
