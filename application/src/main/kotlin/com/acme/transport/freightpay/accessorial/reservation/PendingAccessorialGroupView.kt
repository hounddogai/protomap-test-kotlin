package com.acme.transport.freightpay.accessorial.reservation

/** How screens and exports show a pending accessorial group. */
data class PendingAccessorialGroupView(
    var code: String,
    var name: String,
    var batchSize: String,
    var timezone: String,
    var cost: String,
    var statusLabel: String,
)

fun toPendingAccessorialGroupView(item: PendingAccessorialGroup): PendingAccessorialGroupView {
    return PendingAccessorialGroupView(
        code = item.code,
        name = item.name,
        batchSize = item.batchSize.toString(),
        timezone = item.timezone,
        cost = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingAccessorialGroupViews(items: MutableList<PendingAccessorialGroup>): MutableList<PendingAccessorialGroupView> {
    val views = mutableListOf<PendingAccessorialGroupView>()
    for (item in items) {
        views.add(toPendingAccessorialGroupView(item))
    }
    return views
}

fun sumPendingAccessorialGroupView(view: PendingAccessorialGroupView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedCost = view.cost.toDouble()
    return parsedBatchSize.toDouble() + parsedCost
}
