package com.acme.transport.terminal.traileryard.yardsummary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a trailer yard notice. */
data class TrailerYardNoticeView(
    var code: String,
    var titleText: String,
    var expiresAt: String,
    var palletsText: String,
    var depot: String,
    var statusLabel: String,
)

fun toTrailerYardNoticeView(item: TrailerYardNotice): TrailerYardNoticeView {
    return TrailerYardNoticeView(
        code = item.code,
        titleText = truncate(item.title, 40),
        expiresAt = item.expiresAt.toString(),
        palletsText = item.pallets.toString(),
        depot = truncate(item.depot, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerYardNoticeViews(items: MutableList<TrailerYardNotice>): MutableList<TrailerYardNoticeView> {
    val views = mutableListOf<TrailerYardNoticeView>()
    for (item in items) {
        views.add(toTrailerYardNoticeView(item))
    }
    return views
}

fun sumTrailerYardNoticeView(view: TrailerYardNoticeView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedPallets = view.palletsText.toInt()
    return parsedExpiresAt.toDouble() + parsedPallets.toDouble()
}
