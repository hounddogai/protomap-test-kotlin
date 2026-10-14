package com.acme.transport.customs.tariff.tariffbatch

/** How screens and exports show a capped tariff batch. */
data class CappedTariffBatchView(
    var code: String,
    var title: String,
    var floorValue: String,
    var createdAt: String,
    var sequence: String,
    var budgetText: String,
    var driverId: String,
    var notes: String,
    var phone: String,
    var statusLabel: String,
)

fun toCappedTariffBatchView(item: CappedTariffBatch): CappedTariffBatchView {
    return CappedTariffBatchView(
        code = item.code,
        title = item.title,
        floorValue = "%.2f".format(item.floorValue),
        createdAt = item.createdAt.toString(),
        sequence = item.sequence.toString(),
        budgetText = "%.2f".format(item.budget),
        driverId = item.driverId,
        notes = item.notes,
        phone = item.phone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedTariffBatchViews(items: MutableList<CappedTariffBatch>): MutableList<CappedTariffBatchView> {
    val views = mutableListOf<CappedTariffBatchView>()
    for (item in items) {
        views.add(toCappedTariffBatchView(item))
    }
    return views
}

fun sumCappedTariffBatchView(view: CappedTariffBatchView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedSequence = view.sequence.toInt()
    return parsedFloorValue + parsedCreatedAt.toDouble() + parsedSequence.toDouble()
}
