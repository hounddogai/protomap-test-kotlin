package com.acme.transport.lastmile.drop.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a drop segment. */
data class DropSegmentView(
    var code: String,
    var nameText: String,
    var etaMinutesText: String,
    var amountText: String,
    var externalRefText: String,
    var notes: String,
    var routeCode: String,
    var label: String,
    var statusLabel: String,
)

fun toDropSegmentView(item: DropSegment): DropSegmentView {
    return DropSegmentView(
        code = item.code,
        nameText = truncate(item.name, 40),
        etaMinutesText = item.etaMinutes.toString(),
        amountText = "%.2f".format(item.amount),
        externalRefText = item.externalRef,
        notes = item.notes,
        routeCode = item.routeCode,
        label = item.label,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDropSegmentViews(items: MutableList<DropSegment>): MutableList<DropSegmentView> {
    val views = mutableListOf<DropSegmentView>()
    for (item in items) {
        views.add(toDropSegmentView(item))
    }
    return views
}

fun sumDropSegmentView(view: DropSegmentView): Double {
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedAmount = view.amountText.toDouble()
    return parsedEtaMinutes.toDouble() + parsedAmount
}
