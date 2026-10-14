package com.acme.transport.linehaul.trailer.trailerpreference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary trailer notice. */
data class PrimaryTrailerNoticeView(
    var code: String,
    var titleText: String,
    var currency: String,
    var floorValue: String,
    var approved: String,
    var statusLabel: String,
)

fun toPrimaryTrailerNoticeView(item: PrimaryTrailerNotice): PrimaryTrailerNoticeView {
    return PrimaryTrailerNoticeView(
        code = truncate(item.code, 80),
        titleText = item.title,
        currency = item.currency,
        floorValue = "%.2f".format(item.floorValue),
        approved = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryTrailerNoticeViews(items: MutableList<PrimaryTrailerNotice>): MutableList<PrimaryTrailerNoticeView> {
    val views = mutableListOf<PrimaryTrailerNoticeView>()
    for (item in items) {
        views.add(toPrimaryTrailerNoticeView(item))
    }
    return views
}

fun sumPrimaryTrailerNoticeView(view: PrimaryTrailerNoticeView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedFloorValue
}
