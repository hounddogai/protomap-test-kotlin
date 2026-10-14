package com.acme.transport.freightpay.accessorial.defaultaccessorialadjustment

/** How screens and exports show a rolling accessorial batch. */
data class RollingAccessorialBatchView(
    var code: String,
    var name: String,
    var retries: String,
    var score: String,
    var driverIdText: String,
    var quantity: String,
    var summary: String,
    var statusLabel: String,
)

fun toRollingAccessorialBatchView(item: RollingAccessorialBatch): RollingAccessorialBatchView {
    return RollingAccessorialBatchView(
        code = item.code,
        name = item.name,
        retries = item.retries.toString(),
        score = "%.2f".format(item.score),
        driverIdText = item.driverId,
        quantity = item.quantity.toString(),
        summary = item.summary,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingAccessorialBatchViews(items: MutableList<RollingAccessorialBatch>): MutableList<RollingAccessorialBatchView> {
    val views = mutableListOf<RollingAccessorialBatchView>()
    for (item in items) {
        views.add(toRollingAccessorialBatchView(item))
    }
    return views
}

fun sumRollingAccessorialBatchView(view: RollingAccessorialBatchView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedScore = view.score.toDouble()
    val parsedQuantity = view.quantity.toInt()
    return parsedRetries.toDouble() + parsedScore + parsedQuantity.toDouble()
}
