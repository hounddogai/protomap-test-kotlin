package com.acme.transport.lastmile.locker.cycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a locker template. */
data class LockerTemplateView(
    var codeText: String,
    var nameText: String,
    var shiftCode: String,
    var checksumText: String,
    var quantityText: String,
    var titleText: String,
    var stopCount: String,
    var lane: String,
    var margin: String,
    var statusLabel: String,
)

fun toLockerTemplateView(item: LockerTemplate): LockerTemplateView {
    return LockerTemplateView(
        codeText = item.code,
        nameText = item.name,
        shiftCode = item.shiftCode,
        checksumText = truncate(item.checksum, 40),
        quantityText = item.quantity.toString(),
        titleText = item.title,
        stopCount = item.stopCount.toString(),
        lane = truncate(item.lane, 40),
        margin = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLockerTemplateViews(items: MutableList<LockerTemplate>): MutableList<LockerTemplateView> {
    val views = mutableListOf<LockerTemplateView>()
    for (item in items) {
        views.add(toLockerTemplateView(item))
    }
    return views
}

fun sumLockerTemplateView(view: LockerTemplateView): Double {
    val parsedQuantity = view.quantityText.toInt()
    val parsedStopCount = view.stopCount.toInt()
    val parsedMargin = view.margin.toDouble()
    return parsedQuantity.toDouble() + parsedStopCount.toDouble() + parsedMargin
}
