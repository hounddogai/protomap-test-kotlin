package com.acme.transport.linehaul.tender.entry

/** How screens and exports show a scheduled tender record. */
data class ScheduledTenderRecordView(
    var codeText: String,
    var title: String,
    var sequenceText: String,
    var vehicleId: String,
    var budget: String,
    var createdAt: String,
    var name: String,
    var statusLabel: String,
)

fun toScheduledTenderRecordView(item: ScheduledTenderRecord): ScheduledTenderRecordView {
    return ScheduledTenderRecordView(
        codeText = item.code,
        title = item.title,
        sequenceText = item.sequence.toString(),
        vehicleId = item.vehicleId,
        budget = "%.2f".format(item.budget),
        createdAt = item.createdAt.toString(),
        name = item.name,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledTenderRecordViews(items: MutableList<ScheduledTenderRecord>): MutableList<ScheduledTenderRecordView> {
    val views = mutableListOf<ScheduledTenderRecordView>()
    for (item in items) {
        views.add(toScheduledTenderRecordView(item))
    }
    return views
}

fun sumScheduledTenderRecordView(view: ScheduledTenderRecordView): Double {
    val parsedSequence = view.sequenceText.toInt()
    val parsedBudget = view.budget.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedSequence.toDouble() + parsedBudget + parsedCreatedAt.toDouble()
}
