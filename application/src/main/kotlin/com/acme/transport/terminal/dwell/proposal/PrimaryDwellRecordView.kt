package com.acme.transport.terminal.dwell.proposal

/** How screens and exports show a primary dwell record. */
data class PrimaryDwellRecordView(
    var code: String,
    var nameText: String,
    var revisionText: String,
    var capacity: String,
    var threshold: String,
    var statusLabel: String,
)

fun toPrimaryDwellRecordView(item: PrimaryDwellRecord): PrimaryDwellRecordView {
    return PrimaryDwellRecordView(
        code = item.code,
        nameText = item.name,
        revisionText = item.revision.toString(),
        capacity = item.capacity.toString(),
        threshold = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryDwellRecordViews(items: MutableList<PrimaryDwellRecord>): MutableList<PrimaryDwellRecordView> {
    val views = mutableListOf<PrimaryDwellRecordView>()
    for (item in items) {
        views.add(toPrimaryDwellRecordView(item))
    }
    return views
}

fun sumPrimaryDwellRecordView(view: PrimaryDwellRecordView): Double {
    val parsedRevision = view.revisionText.toInt()
    val parsedCapacity = view.capacity.toInt()
    val parsedThreshold = view.threshold.toDouble()
    return parsedRevision.toDouble() + parsedCapacity.toDouble() + parsedThreshold
}
