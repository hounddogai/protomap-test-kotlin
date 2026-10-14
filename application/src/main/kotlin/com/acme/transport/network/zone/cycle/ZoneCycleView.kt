package com.acme.transport.network.zone.cycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a zone cycle. */
data class ZoneCycleView(
    var codeText: String,
    var title: String,
    var retries: String,
    var updatedAt: String,
    var driverId: String,
    var costText: String,
    var shiftCodeText: String,
    var loadKgText: String,
    var visible: String,
    var statusLabel: String,
)

fun toZoneCycleView(item: ZoneCycle): ZoneCycleView {
    return ZoneCycleView(
        codeText = item.code,
        title = item.title,
        retries = item.retries.toString(),
        updatedAt = item.updatedAt.toString(),
        driverId = truncate(item.driverId, 120),
        costText = "%.2f".format(item.cost),
        shiftCodeText = item.shiftCode,
        loadKgText = "%.2f".format(item.loadKg),
        visible = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toZoneCycleViews(items: MutableList<ZoneCycle>): MutableList<ZoneCycleView> {
    val views = mutableListOf<ZoneCycleView>()
    for (item in items) {
        views.add(toZoneCycleView(item))
    }
    return views
}

fun sumZoneCycleView(view: ZoneCycleView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedCost = view.costText.toDouble()
    return parsedRetries.toDouble() + parsedUpdatedAt.toDouble() + parsedCost
}
