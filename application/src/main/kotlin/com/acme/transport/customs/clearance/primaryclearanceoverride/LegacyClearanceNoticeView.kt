package com.acme.transport.customs.clearance.primaryclearanceoverride

/** How screens and exports show a legacy clearance notice. */
data class LegacyClearanceNoticeView(
    var code: String,
    var labelText: String,
    var title: String,
    var expiresAt: String,
    var notes: String,
    var batchSize: String,
    var amount: String,
    var durationMinutesText: String,
    var statusLabel: String,
)

fun toLegacyClearanceNoticeView(item: LegacyClearanceNotice): LegacyClearanceNoticeView {
    return LegacyClearanceNoticeView(
        code = item.code,
        labelText = item.label,
        title = item.title,
        expiresAt = item.expiresAt.toString(),
        notes = item.notes,
        batchSize = item.batchSize.toString(),
        amount = "%.2f".format(item.amount),
        durationMinutesText = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyClearanceNoticeViews(items: MutableList<LegacyClearanceNotice>): MutableList<LegacyClearanceNoticeView> {
    val views = mutableListOf<LegacyClearanceNoticeView>()
    for (item in items) {
        views.add(toLegacyClearanceNoticeView(item))
    }
    return views
}

fun sumLegacyClearanceNoticeView(view: LegacyClearanceNoticeView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedAmount = view.amount.toDouble()
    return parsedExpiresAt.toDouble() + parsedBatchSize.toDouble() + parsedAmount
}
