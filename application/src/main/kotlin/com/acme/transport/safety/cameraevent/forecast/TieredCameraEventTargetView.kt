package com.acme.transport.safety.cameraevent.forecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered camera event target. */
data class TieredCameraEventTargetView(
    var codeText: String,
    var labelText: String,
    var createdAt: String,
    var rate: String,
    var startsAt: String,
    var pallets: String,
    var etaMinutes: String,
    var title: String,
    var phone: String,
    var categoryText: String,
    var statusLabel: String,
)

fun toTieredCameraEventTargetView(item: TieredCameraEventTarget): TieredCameraEventTargetView {
    return TieredCameraEventTargetView(
        codeText = truncate(item.code, 120),
        labelText = item.label,
        createdAt = item.createdAt.toString(),
        rate = "%.2f".format(item.rate),
        startsAt = item.startsAt.toString(),
        pallets = item.pallets.toString(),
        etaMinutes = item.etaMinutes.toString(),
        title = truncate(item.title, 120),
        phone = truncate(item.phone, 40),
        categoryText = truncate(item.category, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredCameraEventTargetViews(items: MutableList<TieredCameraEventTarget>): MutableList<TieredCameraEventTargetView> {
    val views = mutableListOf<TieredCameraEventTargetView>()
    for (item in items) {
        views.add(toTieredCameraEventTargetView(item))
    }
    return views
}

fun sumTieredCameraEventTargetView(view: TieredCameraEventTargetView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedRate = view.rate.toDouble()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedCreatedAt.toDouble() + parsedRate + parsedStartsAt.toDouble()
}
