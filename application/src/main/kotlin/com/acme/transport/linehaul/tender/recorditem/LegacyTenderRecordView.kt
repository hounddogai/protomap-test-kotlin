package com.acme.transport.linehaul.tender.recorditem

/** How screens and exports show a legacy tender record. */
data class LegacyTenderRecordView(
    var codeText: String,
    var title: String,
    var graceDays: String,
    var version: String,
    var checksumText: String,
    var statusLabel: String,
)

fun toLegacyTenderRecordView(item: LegacyTenderRecord): LegacyTenderRecordView {
    return LegacyTenderRecordView(
        codeText = item.code,
        title = item.title,
        graceDays = item.graceDays.toString(),
        version = item.version.toString(),
        checksumText = item.checksum,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyTenderRecordViews(items: MutableList<LegacyTenderRecord>): MutableList<LegacyTenderRecordView> {
    val views = mutableListOf<LegacyTenderRecordView>()
    for (item in items) {
        views.add(toLegacyTenderRecordView(item))
    }
    return views
}

fun sumLegacyTenderRecordView(view: LegacyTenderRecordView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedVersion = view.version.toInt()
    return parsedGraceDays.toDouble() + parsedVersion.toDouble()
}
