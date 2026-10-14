package com.acme.transport.safety.cameraevent.policy

/** How screens and exports show a regional camera event record. */
data class RegionalCameraEventRecordView(
    var codeText: String,
    var title: String,
    var marginText: String,
    var quantity: String,
    var sequenceText: String,
    var stopCountText: String,
    var createdAtText: String,
    var discount: String,
    var budgetText: String,
    var batchSize: String,
    var statusLabel: String,
)

fun toRegionalCameraEventRecordView(item: RegionalCameraEventRecord): RegionalCameraEventRecordView {
    return RegionalCameraEventRecordView(
        codeText = item.code,
        title = item.title,
        marginText = "%.2f".format(item.margin),
        quantity = item.quantity.toString(),
        sequenceText = item.sequence.toString(),
        stopCountText = item.stopCount.toString(),
        createdAtText = item.createdAt.toString(),
        discount = "%.2f".format(item.discount),
        budgetText = "%.2f".format(item.budget),
        batchSize = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalCameraEventRecordViews(items: MutableList<RegionalCameraEventRecord>): MutableList<RegionalCameraEventRecordView> {
    val views = mutableListOf<RegionalCameraEventRecordView>()
    for (item in items) {
        views.add(toRegionalCameraEventRecordView(item))
    }
    return views
}

fun sumRegionalCameraEventRecordView(view: RegionalCameraEventRecordView): Double {
    val parsedMargin = view.marginText.toDouble()
    val parsedQuantity = view.quantity.toInt()
    val parsedSequence = view.sequenceText.toInt()
    return parsedMargin + parsedQuantity.toDouble() + parsedSequence.toDouble()
}
