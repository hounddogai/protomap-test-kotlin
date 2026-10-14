package com.acme.transport.network.servicearea.claim

/** How screens and exports show a service area record. */
data class ServiceAreaRecordView(
    var code: String,
    var title: String,
    var amount: String,
    var rate: String,
    var capacityText: String,
    var channelText: String,
    var ratio: String,
    var statusLabel: String,
)

fun toServiceAreaRecordView(item: ServiceAreaRecord): ServiceAreaRecordView {
    return ServiceAreaRecordView(
        code = item.code,
        title = item.title,
        amount = "%.2f".format(item.amount),
        rate = "%.2f".format(item.rate),
        capacityText = item.capacity.toString(),
        channelText = item.channel,
        ratio = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServiceAreaRecordViews(items: MutableList<ServiceAreaRecord>): MutableList<ServiceAreaRecordView> {
    val views = mutableListOf<ServiceAreaRecordView>()
    for (item in items) {
        views.add(toServiceAreaRecordView(item))
    }
    return views
}

fun sumServiceAreaRecordView(view: ServiceAreaRecordView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedRate = view.rate.toDouble()
    val parsedCapacity = view.capacityText.toInt()
    return parsedAmount + parsedRate + parsedCapacity.toDouble()
}
