package com.acme.transport.customs.broker.reservation

/** How screens and exports show a broker record. */
data class BrokerRecordView(
    var codeText: String,
    var labelText: String,
    var shiftCode: String,
    var titleText: String,
    var segmentText: String,
    var region: String,
    var quantity: String,
    var statusLabel: String,
)

fun toBrokerRecordView(item: BrokerRecord): BrokerRecordView {
    return BrokerRecordView(
        codeText = item.code,
        labelText = item.label,
        shiftCode = item.shiftCode,
        titleText = item.title,
        segmentText = item.segment,
        region = item.region,
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBrokerRecordViews(items: MutableList<BrokerRecord>): MutableList<BrokerRecordView> {
    val views = mutableListOf<BrokerRecordView>()
    for (item in items) {
        views.add(toBrokerRecordView(item))
    }
    return views
}

fun sumBrokerRecordView(view: BrokerRecordView): Double {
    val parsedQuantity = view.quantity.toInt()
    return parsedQuantity.toDouble()
}
