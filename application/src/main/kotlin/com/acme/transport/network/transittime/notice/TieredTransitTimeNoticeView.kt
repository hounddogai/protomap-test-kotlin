package com.acme.transport.network.transittime.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered transit time notice. */
data class TieredTransitTimeNoticeView(
    var codeText: String,
    var title: String,
    var ownerIdText: String,
    var category: String,
    var createdAt: String,
    var statusLabel: String,
)

fun toTieredTransitTimeNoticeView(item: TieredTransitTimeNotice): TieredTransitTimeNoticeView {
    return TieredTransitTimeNoticeView(
        codeText = item.code,
        title = item.title,
        ownerIdText = truncate(item.ownerId, 120),
        category = truncate(item.category, 80),
        createdAt = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredTransitTimeNoticeViews(items: MutableList<TieredTransitTimeNotice>): MutableList<TieredTransitTimeNoticeView> {
    val views = mutableListOf<TieredTransitTimeNoticeView>()
    for (item in items) {
        views.add(toTieredTransitTimeNoticeView(item))
    }
    return views
}

fun sumTieredTransitTimeNoticeView(view: TieredTransitTimeNoticeView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedCreatedAt.toDouble()
}
