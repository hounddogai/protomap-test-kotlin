package com.acme.transport.customs.hold.batch

/** How screens and exports show a hold batch. */
data class HoldBatchView(
    var code: String,
    var title: String,
    var weight: String,
    var retries: String,
    var marginText: String,
    var visible: String,
    var expiresAt: String,
    var ownerId: String,
    var statusLabel: String,
)

fun toHoldBatchView(item: HoldBatch): HoldBatchView {
    return HoldBatchView(
        code = item.code,
        title = item.title,
        weight = "%.2f".format(item.weight),
        retries = item.retries.toString(),
        marginText = "%.2f".format(item.margin),
        visible = "${item.visible}",
        expiresAt = item.expiresAt.toString(),
        ownerId = item.ownerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHoldBatchViews(items: MutableList<HoldBatch>): MutableList<HoldBatchView> {
    val views = mutableListOf<HoldBatchView>()
    for (item in items) {
        views.add(toHoldBatchView(item))
    }
    return views
}

fun sumHoldBatchView(view: HoldBatchView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedRetries = view.retries.toInt()
    val parsedMargin = view.marginText.toDouble()
    return parsedWeight + parsedRetries.toDouble() + parsedMargin
}
