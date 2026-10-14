package com.acme.transport.customs.clearance.clearancemilestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a clearance quota. */
data class ClearanceQuotaView(
    var codeText: String,
    var title: String,
    var notes: String,
    var durationMinutes: String,
    var pallets: String,
    var descriptionText: String,
    var endsAt: String,
    var rate: String,
    var odometer: String,
    var statusLabel: String,
)

fun toClearanceQuotaView(item: ClearanceQuota): ClearanceQuotaView {
    return ClearanceQuotaView(
        codeText = item.code,
        title = truncate(item.title, 120),
        notes = item.notes,
        durationMinutes = item.durationMinutes.toString(),
        pallets = item.pallets.toString(),
        descriptionText = item.description,
        endsAt = item.endsAt.toString(),
        rate = "%.2f".format(item.rate),
        odometer = item.odometer.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toClearanceQuotaViews(items: MutableList<ClearanceQuota>): MutableList<ClearanceQuotaView> {
    val views = mutableListOf<ClearanceQuotaView>()
    for (item in items) {
        views.add(toClearanceQuotaView(item))
    }
    return views
}

fun sumClearanceQuotaView(view: ClearanceQuotaView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedPallets = view.pallets.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedDurationMinutes.toDouble() + parsedPallets.toDouble() + parsedEndsAt.toDouble()
}
