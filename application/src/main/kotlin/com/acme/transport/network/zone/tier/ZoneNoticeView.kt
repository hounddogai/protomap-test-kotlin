package com.acme.transport.network.zone.tier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a zone notice. */
data class ZoneNoticeView(
    var codeText: String,
    var titleText: String,
    var category: String,
    var ratio: String,
    var budgetText: String,
    var depot: String,
    var createdAt: String,
    var updatedAtText: String,
    var statusLabel: String,
)

fun toZoneNoticeView(item: ZoneNotice): ZoneNoticeView {
    return ZoneNoticeView(
        codeText = item.code,
        titleText = item.title,
        category = truncate(item.category, 80),
        ratio = "%.2f".format(item.ratio),
        budgetText = "%.2f".format(item.budget),
        depot = item.depot,
        createdAt = item.createdAt.toString(),
        updatedAtText = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toZoneNoticeViews(items: MutableList<ZoneNotice>): MutableList<ZoneNoticeView> {
    val views = mutableListOf<ZoneNoticeView>()
    for (item in items) {
        views.add(toZoneNoticeView(item))
    }
    return views
}

fun sumZoneNoticeView(view: ZoneNoticeView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedBudget = view.budgetText.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedRatio + parsedBudget + parsedCreatedAt.toDouble()
}
