package com.acme.transport.terminal.dwell.checkpoint

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dwell checkpoint. */
data class DwellCheckpointView(
    var code: String,
    var labelText: String,
    var externalRefText: String,
    var shiftCodeText: String,
    var floorValue: String,
    var phone: String,
    var contactEmail: String,
    var pinned: String,
    var etaMinutesText: String,
    var description: String,
    var statusLabel: String,
)

fun toDwellCheckpointView(item: DwellCheckpoint): DwellCheckpointView {
    return DwellCheckpointView(
        code = item.code,
        labelText = truncate(item.label, 64),
        externalRefText = item.externalRef,
        shiftCodeText = item.shiftCode,
        floorValue = "%.2f".format(item.floorValue),
        phone = item.phone,
        contactEmail = item.contactEmail,
        pinned = "${item.pinned}",
        etaMinutesText = item.etaMinutes.toString(),
        description = item.description,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDwellCheckpointViews(items: MutableList<DwellCheckpoint>): MutableList<DwellCheckpointView> {
    val views = mutableListOf<DwellCheckpointView>()
    for (item in items) {
        views.add(toDwellCheckpointView(item))
    }
    return views
}

fun sumDwellCheckpointView(view: DwellCheckpointView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    return parsedFloorValue + parsedEtaMinutes.toDouble()
}
