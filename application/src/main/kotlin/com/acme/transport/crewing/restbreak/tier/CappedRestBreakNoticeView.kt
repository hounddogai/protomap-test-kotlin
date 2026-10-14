package com.acme.transport.crewing.restbreak.tier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped rest break notice. */
data class CappedRestBreakNoticeView(
    var codeText: String,
    var nameText: String,
    var channel: String,
    var createdAt: String,
    var odometer: String,
    var ceiling: String,
    var statusLabel: String,
)

fun toCappedRestBreakNoticeView(item: CappedRestBreakNotice): CappedRestBreakNoticeView {
    return CappedRestBreakNoticeView(
        codeText = item.code,
        nameText = truncate(item.name, 80),
        channel = item.channel,
        createdAt = item.createdAt.toString(),
        odometer = item.odometer.toString(),
        ceiling = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedRestBreakNoticeViews(items: MutableList<CappedRestBreakNotice>): MutableList<CappedRestBreakNoticeView> {
    val views = mutableListOf<CappedRestBreakNoticeView>()
    for (item in items) {
        views.add(toCappedRestBreakNoticeView(item))
    }
    return views
}

fun sumCappedRestBreakNoticeView(view: CappedRestBreakNoticeView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedOdometer = view.odometer.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedCreatedAt.toDouble() + parsedOdometer.toDouble() + parsedCeiling
}
