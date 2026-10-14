package com.acme.transport.terminal.sortation.variance

/** How screens and exports show a sortation record. */
data class SortationRecordView(
    var codeText: String,
    var label: String,
    var fuelLitersText: String,
    var approved: String,
    var startsAt: String,
    var retries: String,
    var sequence: String,
    var minimumText: String,
    var statusLabel: String,
)

fun toSortationRecordView(item: SortationRecord): SortationRecordView {
    return SortationRecordView(
        codeText = item.code,
        label = item.label,
        fuelLitersText = "%.2f".format(item.fuelLiters),
        approved = "${item.approved}",
        startsAt = item.startsAt.toString(),
        retries = item.retries.toString(),
        sequence = item.sequence.toString(),
        minimumText = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSortationRecordViews(items: MutableList<SortationRecord>): MutableList<SortationRecordView> {
    val views = mutableListOf<SortationRecordView>()
    for (item in items) {
        views.add(toSortationRecordView(item))
    }
    return views
}

fun sumSortationRecordView(view: SortationRecordView): Double {
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedRetries = view.retries.toInt()
    return parsedFuelLiters + parsedStartsAt.toDouble() + parsedRetries.toDouble()
}
