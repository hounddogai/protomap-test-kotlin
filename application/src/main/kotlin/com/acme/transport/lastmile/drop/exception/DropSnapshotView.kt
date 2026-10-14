package com.acme.transport.lastmile.drop.exception

/** How screens and exports show a drop snapshot. */
data class DropSnapshotView(
    var codeText: String,
    var title: String,
    var driverId: String,
    var unit: String,
    var weight: String,
    var notes: String,
    var sequenceText: String,
    var archived: String,
    var ownerIdText: String,
    var maximum: String,
    var limit: String,
    var statusLabel: String,
)

fun toDropSnapshotView(item: DropSnapshot): DropSnapshotView {
    return DropSnapshotView(
        codeText = item.code,
        title = item.title,
        driverId = item.driverId,
        unit = item.unit,
        weight = "%.2f".format(item.weight),
        notes = item.notes,
        sequenceText = item.sequence.toString(),
        archived = "${item.archived}",
        ownerIdText = item.ownerId,
        maximum = "%.2f".format(item.maximum),
        limit = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDropSnapshotViews(items: MutableList<DropSnapshot>): MutableList<DropSnapshotView> {
    val views = mutableListOf<DropSnapshotView>()
    for (item in items) {
        views.add(toDropSnapshotView(item))
    }
    return views
}

fun sumDropSnapshotView(view: DropSnapshotView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedSequence = view.sequenceText.toInt()
    val parsedMaximum = view.maximum.toDouble()
    return parsedWeight + parsedSequence.toDouble() + parsedMaximum
}
