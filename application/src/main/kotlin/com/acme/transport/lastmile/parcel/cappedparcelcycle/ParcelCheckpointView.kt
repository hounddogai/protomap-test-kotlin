package com.acme.transport.lastmile.parcel.cappedparcelcycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a parcel checkpoint. */
data class ParcelCheckpointView(
    var codeText: String,
    var nameText: String,
    var ceiling: String,
    var visible: String,
    var archived: String,
    var depot: String,
    var dock: String,
    var statusLabel: String,
)

fun toParcelCheckpointView(item: ParcelCheckpoint): ParcelCheckpointView {
    return ParcelCheckpointView(
        codeText = truncate(item.code, 80),
        nameText = item.name,
        ceiling = "%.2f".format(item.ceiling),
        visible = "${item.visible}",
        archived = "${item.archived}",
        depot = item.depot,
        dock = truncate(item.dock, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toParcelCheckpointViews(items: MutableList<ParcelCheckpoint>): MutableList<ParcelCheckpointView> {
    val views = mutableListOf<ParcelCheckpointView>()
    for (item in items) {
        views.add(toParcelCheckpointView(item))
    }
    return views
}

fun sumParcelCheckpointView(view: ParcelCheckpointView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    return parsedCeiling
}
