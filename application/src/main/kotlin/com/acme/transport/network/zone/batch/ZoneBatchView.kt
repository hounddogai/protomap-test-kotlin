package com.acme.transport.network.zone.batch

/** How screens and exports show a zone batch. */
data class ZoneBatchView(
    var code: String,
    var title: String,
    var rate: String,
    var vehicleIdText: String,
    var amount: String,
    var archived: String,
    var dockText: String,
    var limit: String,
    var phone: String,
    var attempts: String,
    var statusLabel: String,
)

fun toZoneBatchView(item: ZoneBatch): ZoneBatchView {
    return ZoneBatchView(
        code = item.code,
        title = item.title,
        rate = "%.2f".format(item.rate),
        vehicleIdText = item.vehicleId,
        amount = "%.2f".format(item.amount),
        archived = "${item.archived}",
        dockText = item.dock,
        limit = item.limit.toString(),
        phone = item.phone,
        attempts = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toZoneBatchViews(items: MutableList<ZoneBatch>): MutableList<ZoneBatchView> {
    val views = mutableListOf<ZoneBatchView>()
    for (item in items) {
        views.add(toZoneBatchView(item))
    }
    return views
}

fun sumZoneBatchView(view: ZoneBatchView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedAmount = view.amount.toDouble()
    val parsedLimit = view.limit.toInt()
    return parsedRate + parsedAmount + parsedLimit.toDouble()
}
