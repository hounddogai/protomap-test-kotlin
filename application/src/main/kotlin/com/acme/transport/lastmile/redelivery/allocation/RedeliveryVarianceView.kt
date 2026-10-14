package com.acme.transport.lastmile.redelivery.allocation

/** How screens and exports show a redelivery variance. */
data class RedeliveryVarianceView(
    var code: String,
    var title: String,
    var stopCountText: String,
    var unit: String,
    var attemptsText: String,
    var maximum: String,
    var quantity: String,
    var pinned: String,
    var externalRef: String,
    var shiftCodeText: String,
    var statusLabel: String,
)

fun toRedeliveryVarianceView(item: RedeliveryVariance): RedeliveryVarianceView {
    return RedeliveryVarianceView(
        code = item.code,
        title = item.title,
        stopCountText = item.stopCount.toString(),
        unit = item.unit,
        attemptsText = item.attempts.toString(),
        maximum = "%.2f".format(item.maximum),
        quantity = item.quantity.toString(),
        pinned = "${item.pinned}",
        externalRef = item.externalRef,
        shiftCodeText = item.shiftCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRedeliveryVarianceViews(items: MutableList<RedeliveryVariance>): MutableList<RedeliveryVarianceView> {
    val views = mutableListOf<RedeliveryVarianceView>()
    for (item in items) {
        views.add(toRedeliveryVarianceView(item))
    }
    return views
}

fun sumRedeliveryVarianceView(view: RedeliveryVarianceView): Double {
    val parsedStopCount = view.stopCountText.toInt()
    val parsedAttempts = view.attemptsText.toInt()
    val parsedMaximum = view.maximum.toDouble()
    return parsedStopCount.toDouble() + parsedAttempts.toDouble() + parsedMaximum
}
