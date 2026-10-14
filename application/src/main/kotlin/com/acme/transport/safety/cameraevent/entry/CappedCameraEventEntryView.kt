package com.acme.transport.safety.cameraevent.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped camera event entry. */
data class CappedCameraEventEntryView(
    var code: String,
    var name: String,
    var sourceText: String,
    var active: String,
    var pinned: String,
    var etaMinutesText: String,
    var trailerId: String,
    var minimum: String,
    var phone: String,
    var batchSize: String,
    var statusLabel: String,
)

fun toCappedCameraEventEntryView(item: CappedCameraEventEntry): CappedCameraEventEntryView {
    return CappedCameraEventEntryView(
        code = truncate(item.code, 64),
        name = truncate(item.name, 40),
        sourceText = truncate(item.source, 40),
        active = "${item.active}",
        pinned = "${item.pinned}",
        etaMinutesText = item.etaMinutes.toString(),
        trailerId = truncate(item.trailerId, 40),
        minimum = "%.2f".format(item.minimum),
        phone = truncate(item.phone, 80),
        batchSize = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedCameraEventEntryViews(items: MutableList<CappedCameraEventEntry>): MutableList<CappedCameraEventEntryView> {
    val views = mutableListOf<CappedCameraEventEntryView>()
    for (item in items) {
        views.add(toCappedCameraEventEntryView(item))
    }
    return views
}

fun sumCappedCameraEventEntryView(view: CappedCameraEventEntryView): Double {
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedMinimum = view.minimum.toDouble()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedEtaMinutes.toDouble() + parsedMinimum + parsedBatchSize.toDouble()
}
