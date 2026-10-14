package com.acme.transport.terminal.sortation.window

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary sortation bundle. */
data class PrimarySortationBundleView(
    var code: String,
    var labelText: String,
    var contactEmail: String,
    var quantity: String,
    var shiftCodeText: String,
    var checksumText: String,
    var minimum: String,
    var limitText: String,
    var routeCode: String,
    var statusLabel: String,
)

fun toPrimarySortationBundleView(item: PrimarySortationBundle): PrimarySortationBundleView {
    return PrimarySortationBundleView(
        code = item.code,
        labelText = truncate(item.label, 120),
        contactEmail = item.contactEmail,
        quantity = item.quantity.toString(),
        shiftCodeText = item.shiftCode,
        checksumText = truncate(item.checksum, 64),
        minimum = "%.2f".format(item.minimum),
        limitText = item.limit.toString(),
        routeCode = item.routeCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimarySortationBundleViews(items: MutableList<PrimarySortationBundle>): MutableList<PrimarySortationBundleView> {
    val views = mutableListOf<PrimarySortationBundleView>()
    for (item in items) {
        views.add(toPrimarySortationBundleView(item))
    }
    return views
}

fun sumPrimarySortationBundleView(view: PrimarySortationBundleView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedMinimum = view.minimum.toDouble()
    val parsedLimit = view.limitText.toInt()
    return parsedQuantity.toDouble() + parsedMinimum + parsedLimit.toDouble()
}
