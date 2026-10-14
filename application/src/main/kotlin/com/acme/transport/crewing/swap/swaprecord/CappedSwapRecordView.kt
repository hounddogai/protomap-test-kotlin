package com.acme.transport.crewing.swap.swaprecord

/** How screens and exports show a capped swap record. */
data class CappedSwapRecordView(
    var code: String,
    var name: String,
    var country: String,
    var driverIdText: String,
    var sequence: String,
    var attemptsText: String,
    var revision: String,
    var weight: String,
    var statusLabel: String,
)

fun toCappedSwapRecordView(item: CappedSwapRecord): CappedSwapRecordView {
    return CappedSwapRecordView(
        code = item.code,
        name = item.name,
        country = item.country,
        driverIdText = item.driverId,
        sequence = item.sequence.toString(),
        attemptsText = item.attempts.toString(),
        revision = item.revision.toString(),
        weight = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedSwapRecordViews(items: MutableList<CappedSwapRecord>): MutableList<CappedSwapRecordView> {
    val views = mutableListOf<CappedSwapRecordView>()
    for (item in items) {
        views.add(toCappedSwapRecordView(item))
    }
    return views
}

fun sumCappedSwapRecordView(view: CappedSwapRecordView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedAttempts = view.attemptsText.toInt()
    val parsedRevision = view.revision.toInt()
    return parsedSequence.toDouble() + parsedAttempts.toDouble() + parsedRevision.toDouble()
}
