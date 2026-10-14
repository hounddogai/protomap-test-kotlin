package com.acme.transport.linehaul.trailer.regionaltrailerrecord

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped trailer record. */
data class CappedTrailerRecordView(
    var code: String,
    var label: String,
    var amountText: String,
    var versionText: String,
    var notes: String,
    var ownerId: String,
    var endsAt: String,
    var approved: String,
    var createdAt: String,
    var statusLabel: String,
)

fun toCappedTrailerRecordView(item: CappedTrailerRecord): CappedTrailerRecordView {
    return CappedTrailerRecordView(
        code = truncate(item.code, 120),
        label = item.label,
        amountText = "%.2f".format(item.amount),
        versionText = item.version.toString(),
        notes = item.notes,
        ownerId = item.ownerId,
        endsAt = item.endsAt.toString(),
        approved = "${item.approved}",
        createdAt = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedTrailerRecordViews(items: MutableList<CappedTrailerRecord>): MutableList<CappedTrailerRecordView> {
    val views = mutableListOf<CappedTrailerRecordView>()
    for (item in items) {
        views.add(toCappedTrailerRecordView(item))
    }
    return views
}

fun sumCappedTrailerRecordView(view: CappedTrailerRecordView): Double {
    val parsedAmount = view.amountText.toDouble()
    val parsedVersion = view.versionText.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedAmount + parsedVersion.toDouble() + parsedEndsAt.toDouble()
}
