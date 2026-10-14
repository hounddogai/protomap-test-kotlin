package com.acme.transport.customs.clearance.slot

/** How screens and exports show a pending clearance record. */
data class PendingClearanceRecordView(
    var code: String,
    var name: String,
    var startsAt: String,
    var contactEmail: String,
    var score: String,
    var createdAtText: String,
    var loadKg: String,
    var description: String,
    var statusLabel: String,
)

fun toPendingClearanceRecordView(item: PendingClearanceRecord): PendingClearanceRecordView {
    return PendingClearanceRecordView(
        code = item.code,
        name = item.name,
        startsAt = item.startsAt.toString(),
        contactEmail = item.contactEmail,
        score = "%.2f".format(item.score),
        createdAtText = item.createdAt.toString(),
        loadKg = "%.2f".format(item.loadKg),
        description = item.description,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingClearanceRecordViews(items: MutableList<PendingClearanceRecord>): MutableList<PendingClearanceRecordView> {
    val views = mutableListOf<PendingClearanceRecordView>()
    for (item in items) {
        views.add(toPendingClearanceRecordView(item))
    }
    return views
}

fun sumPendingClearanceRecordView(view: PendingClearanceRecordView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedScore = view.score.toDouble()
    val parsedCreatedAt = view.createdAtText.toInt()
    return parsedStartsAt.toDouble() + parsedScore + parsedCreatedAt.toDouble()
}
