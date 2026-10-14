package com.acme.transport.network.zone.baseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient zone notice. */
data class LenientZoneNoticeView(
    var code: String,
    var nameText: String,
    var country: String,
    var endsAtText: String,
    var locale: String,
    var channel: String,
    var reviewer: String,
    var expiresAt: String,
    var source: String,
    var routeCodeText: String,
    var statusLabel: String,
)

fun toLenientZoneNoticeView(item: LenientZoneNotice): LenientZoneNoticeView {
    return LenientZoneNoticeView(
        code = item.code,
        nameText = item.name,
        country = truncate(item.country, 64),
        endsAtText = item.endsAt.toString(),
        locale = item.locale,
        channel = item.channel,
        reviewer = item.reviewer,
        expiresAt = item.expiresAt.toString(),
        source = truncate(item.source, 80),
        routeCodeText = truncate(item.routeCode, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientZoneNoticeViews(items: MutableList<LenientZoneNotice>): MutableList<LenientZoneNoticeView> {
    val views = mutableListOf<LenientZoneNoticeView>()
    for (item in items) {
        views.add(toLenientZoneNoticeView(item))
    }
    return views
}

fun sumLenientZoneNoticeView(view: LenientZoneNoticeView): Double {
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedEndsAt.toDouble() + parsedExpiresAt.toDouble()
}
