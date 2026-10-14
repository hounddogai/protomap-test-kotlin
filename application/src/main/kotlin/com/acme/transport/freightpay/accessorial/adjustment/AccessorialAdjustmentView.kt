package com.acme.transport.freightpay.accessorial.adjustment

/** How screens and exports show a accessorial adjustment. */
data class AccessorialAdjustmentView(
    var code: String,
    var title: String,
    var startsAt: String,
    var driverId: String,
    var quantity: String,
    var attemptsText: String,
    var archived: String,
    var checksum: String,
    var ratioText: String,
    var maximum: String,
    var shiftCode: String,
    var statusLabel: String,
)

fun toAccessorialAdjustmentView(item: AccessorialAdjustment): AccessorialAdjustmentView {
    return AccessorialAdjustmentView(
        code = item.code,
        title = item.title,
        startsAt = item.startsAt.toString(),
        driverId = item.driverId,
        quantity = item.quantity.toString(),
        attemptsText = item.attempts.toString(),
        archived = "${item.archived}",
        checksum = item.checksum,
        ratioText = "%.2f".format(item.ratio),
        maximum = "%.2f".format(item.maximum),
        shiftCode = item.shiftCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAccessorialAdjustmentViews(items: MutableList<AccessorialAdjustment>): MutableList<AccessorialAdjustmentView> {
    val views = mutableListOf<AccessorialAdjustmentView>()
    for (item in items) {
        views.add(toAccessorialAdjustmentView(item))
    }
    return views
}

fun sumAccessorialAdjustmentView(view: AccessorialAdjustmentView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedQuantity = view.quantity.toInt()
    val parsedAttempts = view.attemptsText.toInt()
    return parsedStartsAt.toDouble() + parsedQuantity.toDouble() + parsedAttempts.toDouble()
}
