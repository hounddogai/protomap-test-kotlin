package com.acme.transport.lastmile.drop.dropallocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient drop notice. */
data class LenientDropNoticeView(
    var codeText: String,
    var label: String,
    var laneText: String,
    var category: String,
    var graceDays: String,
    var retries: String,
    var statusLabel: String,
)

fun toLenientDropNoticeView(item: LenientDropNotice): LenientDropNoticeView {
    return LenientDropNoticeView(
        codeText = item.code,
        label = item.label,
        laneText = truncate(item.lane, 64),
        category = item.category,
        graceDays = item.graceDays.toString(),
        retries = item.retries.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientDropNoticeViews(items: MutableList<LenientDropNotice>): MutableList<LenientDropNoticeView> {
    val views = mutableListOf<LenientDropNoticeView>()
    for (item in items) {
        views.add(toLenientDropNoticeView(item))
    }
    return views
}

fun sumLenientDropNoticeView(view: LenientDropNoticeView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedRetries = view.retries.toInt()
    return parsedGraceDays.toDouble() + parsedRetries.toDouble()
}
