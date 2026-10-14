package com.acme.transport.crewing.swap.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled swap quota. */
data class ScheduledSwapQuotaView(
    var code: String,
    var title: String,
    var notes: String,
    var maximum: String,
    var routeCode: String,
    var archived: String,
    var unit: String,
    var updatedAtText: String,
    var statusLabel: String,
)

fun toScheduledSwapQuotaView(item: ScheduledSwapQuota): ScheduledSwapQuotaView {
    return ScheduledSwapQuotaView(
        code = item.code,
        title = truncate(item.title, 80),
        notes = item.notes,
        maximum = "%.2f".format(item.maximum),
        routeCode = item.routeCode,
        archived = "${item.archived}",
        unit = item.unit,
        updatedAtText = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledSwapQuotaViews(items: MutableList<ScheduledSwapQuota>): MutableList<ScheduledSwapQuotaView> {
    val views = mutableListOf<ScheduledSwapQuotaView>()
    for (item in items) {
        views.add(toScheduledSwapQuotaView(item))
    }
    return views
}

fun sumScheduledSwapQuotaView(view: ScheduledSwapQuotaView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedUpdatedAt = view.updatedAtText.toInt()
    return parsedMaximum + parsedUpdatedAt.toDouble()
}
