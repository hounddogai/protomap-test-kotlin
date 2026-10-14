package com.acme.transport.customs.clearance.overrideitem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a clearance schedule. */
data class ClearanceScheduleView(
    var code: String,
    var nameText: String,
    var quantity: String,
    var visibleText: String,
    var routeCode: String,
    var etaMinutes: String,
    var margin: String,
    var score: String,
    var descriptionText: String,
    var currency: String,
    var statusLabel: String,
)

fun toClearanceScheduleView(item: ClearanceSchedule): ClearanceScheduleView {
    return ClearanceScheduleView(
        code = item.code,
        nameText = item.name,
        quantity = item.quantity.toString(),
        visibleText = "${item.visible}",
        routeCode = truncate(item.routeCode, 40),
        etaMinutes = item.etaMinutes.toString(),
        margin = "%.2f".format(item.margin),
        score = "%.2f".format(item.score),
        descriptionText = item.description,
        currency = item.currency,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toClearanceScheduleViews(items: MutableList<ClearanceSchedule>): MutableList<ClearanceScheduleView> {
    val views = mutableListOf<ClearanceScheduleView>()
    for (item in items) {
        views.add(toClearanceScheduleView(item))
    }
    return views
}

fun sumClearanceScheduleView(view: ClearanceScheduleView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedMargin = view.margin.toDouble()
    return parsedQuantity.toDouble() + parsedEtaMinutes.toDouble() + parsedMargin
}
