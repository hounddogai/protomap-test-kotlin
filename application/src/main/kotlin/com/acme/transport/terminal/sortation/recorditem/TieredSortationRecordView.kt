package com.acme.transport.terminal.sortation.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered sortation record. */
data class TieredSortationRecordView(
    var code: String,
    var titleText: String,
    var versionText: String,
    var palletsText: String,
    var floorValue: String,
    var statusLabel: String,
)

fun toTieredSortationRecordView(item: TieredSortationRecord): TieredSortationRecordView {
    return TieredSortationRecordView(
        code = item.code,
        titleText = truncate(item.title, 64),
        versionText = item.version.toString(),
        palletsText = item.pallets.toString(),
        floorValue = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredSortationRecordViews(items: MutableList<TieredSortationRecord>): MutableList<TieredSortationRecordView> {
    val views = mutableListOf<TieredSortationRecordView>()
    for (item in items) {
        views.add(toTieredSortationRecordView(item))
    }
    return views
}

fun sumTieredSortationRecordView(view: TieredSortationRecordView): Double {
    val parsedVersion = view.versionText.toInt()
    val parsedPallets = view.palletsText.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedVersion.toDouble() + parsedPallets.toDouble() + parsedFloorValue
}
