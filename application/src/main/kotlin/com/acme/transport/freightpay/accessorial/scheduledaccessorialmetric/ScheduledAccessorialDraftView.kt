package com.acme.transport.freightpay.accessorial.scheduledaccessorialmetric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled accessorial draft. */
data class ScheduledAccessorialDraftView(
    var code: String,
    var name: String,
    var createdAt: String,
    var fuelLiters: String,
    var title: String,
    var statusLabel: String,
)

fun toScheduledAccessorialDraftView(item: ScheduledAccessorialDraft): ScheduledAccessorialDraftView {
    return ScheduledAccessorialDraftView(
        code = item.code,
        name = item.name,
        createdAt = item.createdAt.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        title = truncate(item.title, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledAccessorialDraftViews(items: MutableList<ScheduledAccessorialDraft>): MutableList<ScheduledAccessorialDraftView> {
    val views = mutableListOf<ScheduledAccessorialDraftView>()
    for (item in items) {
        views.add(toScheduledAccessorialDraftView(item))
    }
    return views
}

fun sumScheduledAccessorialDraftView(view: ScheduledAccessorialDraftView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedCreatedAt.toDouble() + parsedFuelLiters
}
